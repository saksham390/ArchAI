package com.archai.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiChatRequest(
    @NotBlank @Size(max = 6000) String message,
    @Size(max = 12000) String context
) {}
