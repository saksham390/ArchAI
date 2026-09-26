package com.archai.ai.dto;

import java.util.List;

public record AiReviewResponse(int score, String summary, List<AiReviewFinding> findings, List<KnowledgeSource> sources) {}
