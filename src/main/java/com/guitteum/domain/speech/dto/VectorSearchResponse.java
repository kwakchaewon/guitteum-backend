package com.guitteum.domain.speech.dto;

public record VectorSearchResponse(
        Long chunkId,
        Long speechId,
        int chunkIndex,
        String content,
        float score
) {
}
