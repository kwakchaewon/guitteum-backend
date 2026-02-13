package com.guitteum.domain.chat.dto;

import com.guitteum.domain.chat.entity.ChatMessage;
import com.guitteum.domain.chat.entity.MessageSource;

import java.time.LocalDateTime;
import java.util.List;

public record ChatMessageResponse(
        String role,
        String content,
        LocalDateTime createdAt,
        List<SourceInfo> sources
) {
    public record SourceInfo(
            Long speechId,
            Long chunkId,
            Float relevanceScore
    ) {
        public static SourceInfo from(MessageSource source) {
            return new SourceInfo(source.getSpeechId(), source.getChunkId(), source.getRelevanceScore());
        }
    }

    public static ChatMessageResponse from(ChatMessage message) {
        List<SourceInfo> sourceInfos = message.getSources().stream()
                .map(SourceInfo::from)
                .toList();
        return new ChatMessageResponse(
                message.getRole().name().toLowerCase(),
                message.getContent(),
                message.getCreatedAt(),
                sourceInfos
        );
    }
}
