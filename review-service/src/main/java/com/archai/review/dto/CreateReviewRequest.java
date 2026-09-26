package com.archai.review.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReviewRequest(
    @NotNull Long designId,
    @Size(max = 12000) String requirements
) {}
