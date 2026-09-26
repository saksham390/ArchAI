package com.archai.ai.dto;

import java.util.List;

public record AiChatResponse(String reply, List<KnowledgeSource> sources) {}
