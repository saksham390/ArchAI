package com.archai.knowledge.dto;

import com.archai.knowledge.entity.KnowledgeDocument;
import java.time.Instant;

public record KnowledgeDocumentResponse(Long id, String title, String content, String sourceUrl, Instant createdAt) {
    public static KnowledgeDocumentResponse from(KnowledgeDocument document) {
        return new KnowledgeDocumentResponse(
            document.getId(),
            document.getTitle(),
            document.getContent(),
            document.getSourceUrl(),
            document.getCreatedAt()
        );
    }
}
