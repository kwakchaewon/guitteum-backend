package com.guitteum.domain.chat.service;

import com.guitteum.domain.speech.dto.VectorSearchResponse;
import com.guitteum.domain.speech.service.VectorSearchService;
import com.guitteum.infra.gemini.GeminiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final VectorSearchService vectorSearchService;
    private final GeminiClient geminiClient;

    private static final String SYSTEM_PROMPT = """
            당신은 대한민국 대통령 연설문 전문가입니다.
            제공된 연설문 내용을 바탕으로 정확하고 객관적으로 답변하세요.
            연설문에 없는 내용은 추측하지 마세요.
            답변 시 참고한 연설문을 명시하세요.
            한국어로 답변하세요.""";

    private static final int TOP_K = 5;

    public RagResult ask(String query, List<GeminiClient.ChatMessage> history) {
        // 1. 벡터 검색으로 관련 청크 조회
        List<VectorSearchResponse> searchResults = vectorSearchService.search(query, TOP_K);

        if (searchResults.isEmpty()) {
            return new RagResult("관련된 연설문을 찾을 수 없습니다.", List.of());
        }

        List<GeminiClient.ChatMessage> messages = buildMessages(query, searchResults, history);

        String answer = geminiClient.chat(messages);

        log.info("RAG query='{}', chunks={}, answer length={}", query, searchResults.size(), answer.length());

        return new RagResult(answer, searchResults);
    }

    public List<VectorSearchResponse> askStream(String query, List<GeminiClient.ChatMessage> history,
                                                 Consumer<String> tokenConsumer) {
        List<VectorSearchResponse> searchResults = vectorSearchService.search(query, TOP_K);

        if (searchResults.isEmpty()) {
            tokenConsumer.accept("관련된 연설문을 찾을 수 없습니다.");
            return List.of();
        }

        List<GeminiClient.ChatMessage> messages = buildMessages(query, searchResults, history);

        geminiClient.chatStream(messages, tokenConsumer);

        log.info("RAG stream query='{}', chunks={}", query, searchResults.size());

        return searchResults;
    }

    private List<GeminiClient.ChatMessage> buildMessages(String query,
                                                          List<VectorSearchResponse> searchResults,
                                                          List<GeminiClient.ChatMessage> history) {
        String context = searchResults.stream()
                .map(r -> "[연설문 ID: %d, 청크 %d]\n%s".formatted(r.speechId(), r.chunkIndex(), r.content()))
                .collect(Collectors.joining("\n\n---\n\n"));

        String userMessage = """
                다음 연설문 내용을 참고하여 질문에 답변하세요.

                [참고 연설문]
                %s

                [질문]
                %s""".formatted(context, query);

        List<GeminiClient.ChatMessage> messages = new ArrayList<>();
        messages.add(new GeminiClient.ChatMessage("system", SYSTEM_PROMPT));
        messages.addAll(history);
        messages.add(new GeminiClient.ChatMessage("user", userMessage));
        return messages;
    }

    public record RagResult(
            String answer,
            List<VectorSearchResponse> sources
    ) {}
}
