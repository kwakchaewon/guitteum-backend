package com.guitteum.infra.openai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiClient {

    private final RestClient openAiRestClient;

    @Value("${openai.model}")
    private String embeddingModel;

    @Value("${openai.chat-model}")
    private String chatModel;

    public float[] embed(String text) {
        List<float[]> results = embedBatch(List.of(text));
        return results.get(0);
    }

    public List<float[]> embedBatch(List<String> texts) {
        EmbeddingRequest request = new EmbeddingRequest(embeddingModel, texts);

        EmbeddingResponse response = openAiRestClient.post()
                .uri("/embeddings")
                .body(request)
                .retrieve()
                .body(EmbeddingResponse.class);

        if (response == null || response.data() == null) {
            throw new RuntimeException("Empty response from OpenAI embedding API");
        }

        log.debug("Embedded {} texts, usage: {} tokens", texts.size(), response.usage().totalTokens());

        return response.data().stream()
                .map(EmbeddingData::embedding)
                .toList();
    }

    // --- Chat Completion ---

    public String chat(List<ChatMessage> messages) {
        ChatRequest request = new ChatRequest(chatModel, messages, 0.7);

        ChatCompletionResponse response = openAiRestClient.post()
                .uri("/chat/completions")
                .body(request)
                .retrieve()
                .body(ChatCompletionResponse.class);

        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new RuntimeException("Empty response from OpenAI chat API");
        }

        String content = response.choices().get(0).message().content();
        log.debug("Chat completion usage: {} tokens", response.usage().totalTokens());
        return content;
    }

    // --- Records ---

    record EmbeddingRequest(String model, List<String> input) {}

    record EmbeddingResponse(List<EmbeddingData> data, Usage usage) {}

    record EmbeddingData(int index, float[] embedding) {}

    public record ChatMessage(String role, String content) {}

    record ChatRequest(String model, List<ChatMessage> messages, double temperature) {}

    record ChatCompletionResponse(List<Choice> choices, Usage usage) {}

    record Choice(ChatMessage message) {}

    record Usage(
            @JsonProperty("prompt_tokens") int promptTokens,
            @JsonProperty("total_tokens") int totalTokens
    ) {}
}
