package com.archai.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiReviewRequest(
    @NotBlank @Size(max = 160) String title,
    @NotBlank @Size(max = 12000) String requirements,
    @NotBlank @Size(max = 30000) String architectureDiagram
) {}
