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
    private String model;

    public float[] embed(String text) {
        List<float[]> results = embedBatch(List.of(text));
        return results.get(0);
    }

    public List<float[]> embedBatch(List<String> texts) {
        EmbeddingRequest request = new EmbeddingRequest(model, texts);

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

    record EmbeddingRequest(String model, List<String> input) {}

    record EmbeddingResponse(List<EmbeddingData> data, Usage usage) {}

    record EmbeddingData(int index, float[] embedding) {}

    record Usage(
            @JsonProperty("prompt_tokens") int promptTokens,
            @JsonProperty("total_tokens") int totalTokens
    ) {}
}
