package com.archai.ai.dto;

import java.util.List;

public record AiGenerationResponse(
    String title,
    String summary,
    String architectureDiagram,
    List<String> tradeoffs,
    List<KnowledgeSource> sources
) {}
