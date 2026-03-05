package com.guitteum.infra.gemini;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import jakarta.annotation.PostConstruct;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiClient {

    private final RestClient geminiRestClient;
    private final RestClient geminiEmbeddingRestClient;

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.embedding-model}")
    private String embeddingModel;

    @Value("${gemini.chat-model}")
    private String chatModel;

    @Value("${gemini.base-url}")
    private String baseUrl;

    @PostConstruct
    void init() {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("GEMINI_API_KEY is not set! All Gemini API calls will fail with 403.");
        } else {
            log.info("Gemini API key loaded (length={})", apiKey.length());
        }
    }

    // ===== Public API =====

    public float[] embed(String text) {
        List<float[]> results = embedBatch(List.of(text));
        return results.get(0);
    }

    public List<float[]> embedBatch(List<String> texts) {
        String modelPath = "models/" + embeddingModel;

        List<EmbedRequest> requests = texts.stream()
                .map(text -> new EmbedRequest(modelPath, new Content(List.of(new Part(text)))))
                .toList();

        BatchEmbedRequest request = new BatchEmbedRequest(requests);

        BatchEmbedResponse response = geminiEmbeddingRestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/models/{model}:batchEmbedContents")
                        .queryParam("key", apiKey)
                        .build(embeddingModel))
                .body(request)
                .retrieve()
                .body(BatchEmbedResponse.class);

        if (response == null || response.embeddings() == null) {
            throw new RuntimeException("Empty response from Gemini embedding API");
        }

        log.debug("Embedded {} texts via Gemini", texts.size());

        return response.embeddings().stream()
                .map(Embedding::values)
                .toList();
    }

    public String chat(List<ChatMessage> messages) {
        GenerateContentRequest request = buildChatRequest(messages);

        GenerateContentResponse response = geminiRestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/models/{model}:generateContent")
                        .queryParam("key", apiKey)
                        .build(chatModel))
                .body(request)
                .retrieve()
                .body(GenerateContentResponse.class);

        if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
            throw new RuntimeException("Empty response from Gemini chat API");
        }

        String content = response.candidates().get(0).content().parts().get(0).text();
        log.debug("Gemini chat completed");
        return content;
    }

    public void chatStream(List<ChatMessage> messages, Consumer<String> tokenConsumer) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            GenerateContentRequest request = buildChatRequest(messages);
            String requestBody = mapper.writeValueAsString(request);

            String url = baseUrl + "/models/" + chatModel + ":streamGenerateContent?alt=sse&key=" + apiKey;

            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<java.io.InputStream> response = httpClient.send(
                    httpRequest, HttpResponse.BodyHandlers.ofInputStream());

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6).trim();
                        if (data.isEmpty()) continue;
                        JsonNode node = mapper.readTree(data);
                        JsonNode parts = node.path("candidates").path(0)
                                .path("content").path("parts");
                        if (parts.isArray() && !parts.isEmpty()) {
                            JsonNode textNode = parts.get(0).path("text");
                            if (!textNode.isMissingNode() && !textNode.isNull()) {
                                tokenConsumer.accept(textNode.asText());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Gemini streaming failed", e);
        }
    }

    // ===== Internal helpers =====

    private GenerateContentRequest buildChatRequest(List<ChatMessage> messages) {
        Content systemInstruction = null;
        List<GeminiContent> contents = new ArrayList<>();

        for (ChatMessage msg : messages) {
            if ("system".equals(msg.role())) {
                systemInstruction = new Content(List.of(new Part(msg.content())));
            } else {
                String geminiRole = "assistant".equals(msg.role()) ? "model" : msg.role();
                contents.add(new GeminiContent(geminiRole, List.of(new Part(msg.content()))));
            }
        }

        return new GenerateContentRequest(
                systemInstruction,
                contents,
                new GenerationConfig(0.7)
        );
    }

    // ===== Public record (shared with domain layer) =====

    public record ChatMessage(String role, String content) {}

    // ===== Private Gemini API records =====

    // Embedding
    private record Part(String text) {}
    private record Content(List<Part> parts) {}
    private record EmbedRequest(String model, Content content) {}
    private record BatchEmbedRequest(List<EmbedRequest> requests) {}
    private record Embedding(float[] values) {}
    private record BatchEmbedResponse(List<Embedding> embeddings) {}

    // Chat
    private record GeminiContent(String role, List<Part> parts) {}
    private record GenerationConfig(double temperature) {}

    private record GenerateContentRequest(
            @JsonProperty("system_instruction") Content systemInstruction,
            List<GeminiContent> contents,
            GenerationConfig generationConfig
    ) {}

    private record Candidate(GeminiContent content) {}
    private record GenerateContentResponse(List<Candidate> candidates) {}
}
