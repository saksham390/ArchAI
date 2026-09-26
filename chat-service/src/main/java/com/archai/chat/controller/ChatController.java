package com.archai.chat.controller;

import com.archai.chat.dto.ChatConversationResponse;
import com.archai.chat.dto.ChatMessageResponse;
import com.archai.chat.dto.CreateConversationRequest;
import com.archai.chat.dto.SendMessageRequest;
import com.archai.chat.service.ChatApplicationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat/conversations")
public class ChatController {
    private final ChatApplicationService chatService;

    public ChatController(ChatApplicationService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    public List<ChatConversationResponse> list(@RequestHeader("X-User-Id") String ownerId) {
        return chatService.list(ownerId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChatConversationResponse create(
        @RequestHeader("X-User-Id") String ownerId,
        @Valid @RequestBody CreateConversationRequest request
    ) {
        return chatService.create(ownerId, request);
    }

    @GetMapping("/{id}/messages")
    public List<ChatMessageResponse> messages(
        @PathVariable Long id,
        @RequestHeader("X-User-Id") String ownerId
    ) {
        return chatService.messages(id, ownerId);
    }

    @PostMapping("/{id}/messages")
    public ChatMessageResponse send(
        @PathVariable Long id,
        @RequestHeader("X-User-Id") String ownerId,
        @Valid @RequestBody SendMessageRequest request
    ) {
        return chatService.send(id, ownerId, request);
    }
}
