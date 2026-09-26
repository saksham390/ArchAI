package com.archai.chat.dto;

import com.archai.chat.entity.ChatConversation;
import java.time.Instant;

public record ChatConversationResponse(Long id, String title, Instant createdAt, Instant updatedAt) {
    public static ChatConversationResponse from(ChatConversation conversation) {
        return new ChatConversationResponse(
            conversation.getId(),
            conversation.getTitle(),
            conversation.getCreatedAt(),
            conversation.getUpdatedAt()
        );
    }
}
