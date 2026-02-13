package com.guitteum.domain.speech.dto;

import com.guitteum.domain.speech.entity.Speech;

import java.time.LocalDateTime;

public record SpeechDetailResponse(
        Long id,
        String title,
        String content,
        LocalDateTime speechDate,
        String eventName,
        LocalDateTime createdAt
) {
    public static SpeechDetailResponse from(Speech speech) {
        return new SpeechDetailResponse(
                speech.getId(),
                speech.getTitle(),
                speech.getContent(),
                speech.getSpeechDate(),
                speech.getEventName(),
                speech.getCreatedAt()
        );
    }
}
