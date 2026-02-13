package com.guitteum.domain.speech.service;

import com.guitteum.domain.speech.dto.VectorSearchResponse;
import com.guitteum.infra.openai.OpenAiClient;
import com.guitteum.infra.qdrant.QdrantClientWrapper;
import io.qdrant.client.grpc.Points;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorSearchService {

    private final OpenAiClient openAiClient;
    private final QdrantClientWrapper qdrantClientWrapper;

    public List<VectorSearchResponse> search(String query, int topK) {
        float[] queryVector = openAiClient.embed(query);

        List<Points.ScoredPoint> results = qdrantClientWrapper.search(queryVector, topK);

        return results.stream()
                .map(this::toResponse)
                .toList();
    }

    private VectorSearchResponse toResponse(Points.ScoredPoint point) {
        var payload = point.getPayloadMap();

        long speechId = payload.containsKey("speechId")
                ? payload.get("speechId").getIntegerValue() : 0L;
        int chunkIndex = payload.containsKey("chunkIndex")
                ? (int) payload.get("chunkIndex").getIntegerValue() : 0;
        String content = payload.containsKey("content")
                ? payload.get("content").getStringValue() : "";

        return new VectorSearchResponse(
                null,
                speechId,
                chunkIndex,
                content,
                point.getScore()
        );
    }
}
