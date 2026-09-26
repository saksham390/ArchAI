package com.archai.ai.dto;

public record AiReviewFinding(
    String severity,
    String category,
    String title,
    String detail,
    String recommendation
) {}
