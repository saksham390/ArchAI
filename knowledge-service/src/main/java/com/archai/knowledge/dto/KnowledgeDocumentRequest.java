package com.archai.knowledge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record KnowledgeDocumentRequest(
    @NotBlank @Size(max = 200) String title,
    @NotBlank @Size(max = 50000) String content,
    @Size(max = 2048) String sourceUrl
) {}
