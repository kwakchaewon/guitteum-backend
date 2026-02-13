package com.guitteum.domain.chat.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        String sessionId,
        @NotBlank(message = "메시지는 필수입니다")
        String message
) {
}
