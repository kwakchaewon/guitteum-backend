package com.guitteum.domain.speech.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SpeechSearchResponse(
        Long id,
        String title,
        LocalDateTime speechDate,
        String eventName,
        String category,
        List<String> titleHighlights,
        List<String> contentHighlights
) {
}
