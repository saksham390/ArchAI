package com.archai.chat.dto;

import com.archai.chat.entity.ChatMessage;
import java.time.Instant;

public record ChatMessageResponse(Long id, String role, String content, Instant createdAt) {
    public static ChatMessageResponse from(ChatMessage message) {
        return new ChatMessageResponse(message.getId(), message.getRole(), message.getContent(), message.getCreatedAt());
    }
}
