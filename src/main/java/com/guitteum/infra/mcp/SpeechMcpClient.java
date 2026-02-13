package com.guitteum.infra.mcp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class SpeechMcpClient {

    @Value("${data-go.api-key}")
    private String apiKey;

    @Value("${data-go.mcp-command}")
    private String mcpCommand;

    @Value("${data-go.mcp-args}")
    private String mcpArgs;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<SpeechData> listSpeeches(int page, int perPage) {
        McpSyncClient client = createClient();
        try {
            client.initialize();

            CallToolResult result = client.callTool(new CallToolRequest(
                    "list_speeches",
                    Map.of("page", page, "per_page", perPage)
            ));

            return parseResult(result);
        } finally {
            client.closeGracefully();
        }
    }

    public List<SpeechData> searchSpeeches(String president, int page, int perPage) {
        McpSyncClient client = createClient();
        try {
            client.initialize();

            CallToolResult result = client.callTool(new CallToolRequest(
                    "search_speeches",
                    Map.of("president", president, "page", page, "per_page", perPage)
            ));

            return parseResult(result);
        } finally {
            client.closeGracefully();
        }
    }

    /**
     * 전체 연설문을 페이지 단위로 수집.
     * MCP 서버를 한 번만 초기화하고 모든 페이지를 처리한 뒤 종료.
     */
    public int collectAll(int perPage, SpeechDataConsumer consumer) {
        McpSyncClient client = createClient();
        int totalCollected = 0;
        try {
            client.initialize();
            log.info("MCP 서버 초기화 완료. 연설문 수집 시작 (perPage={})", perPage);

            int page = 1;
            while (true) {
                CallToolResult result = client.callTool(new CallToolRequest(
                        "list_speeches",
                        Map.of("page", page, "per_page", perPage)
                ));

                List<SpeechData> speeches = parseResult(result);
                if (speeches.isEmpty()) {
                    break;
                }

                consumer.accept(speeches);
                totalCollected += speeches.size();
                log.info("Page {} 수집 완료: {}건 (누적 {}건)", page, speeches.size(), totalCollected);

                if (speeches.size() < perPage) {
                    break;
                }
                page++;
            }

            log.info("연설문 수집 완료. 총 {}건", totalCollected);
            return totalCollected;
        } finally {
            client.closeGracefully();
        }
    }

    private McpSyncClient createClient() {
        ServerParameters params = ServerParameters.builder(mcpCommand)
                .args(mcpArgs)
                .addEnvVar("API_KEY", apiKey)
                .build();

        StdioClientTransport transport = new StdioClientTransport(params, new JacksonMcpJsonMapper(new ObjectMapper()));

        return McpClient.sync(transport)
                .requestTimeout(Duration.ofSeconds(30))
                .build();
    }

    private List<SpeechData> parseResult(CallToolResult result) {
        try {
            String text = result.content().stream()
                    .filter(c -> c instanceof McpSchema.TextContent)
                    .map(c -> ((McpSchema.TextContent) c).text())
                    .findFirst()
                    .orElse("[]");

            // MCP 응답이 JSON 배열이면 직접 파싱, 아니면 data 필드 추출
            if (text.trim().startsWith("[")) {
                return objectMapper.readValue(text, new TypeReference<>() {});
            }

            // { "data": [...] } 형태
            Map<String, Object> wrapper = objectMapper.readValue(text, new TypeReference<>() {});
            Object data = wrapper.get("data");
            if (data != null) {
                String dataJson = objectMapper.writeValueAsString(data);
                return objectMapper.readValue(dataJson, new TypeReference<>() {});
            }

            return List.of();
        } catch (Exception e) {
            log.error("MCP 응답 파싱 실패: {}", e.getMessage());
            return List.of();
        }
    }

    @FunctionalInterface
    public interface SpeechDataConsumer {
        void accept(List<SpeechData> speeches);
    }
}
