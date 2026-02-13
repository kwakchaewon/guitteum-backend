package com.guitteum.domain.speech.dto;

import com.guitteum.domain.speech.entity.Speech;

import java.time.LocalDateTime;

public record SpeechResponse(
        Long id,
        String title,
        LocalDateTime speechDate,
        String eventName
) {
    public static SpeechResponse from(Speech speech) {
        return new SpeechResponse(
                speech.getId(),
                speech.getTitle(),
                speech.getSpeechDate(),
                speech.getEventName()
        );
    }
}
