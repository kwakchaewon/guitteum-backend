package com.guitteum.domain.chat.dto;

import java.util.List;

public record ChatResponse(
        String sessionId,
        String answer,
        List<SourceInfo> sources
) {
    public record SourceInfo(
            Long speechId,
            int chunkIndex,
            String content,
            float score
    ) {}
}
