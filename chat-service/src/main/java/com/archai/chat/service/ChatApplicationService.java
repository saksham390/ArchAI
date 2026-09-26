package com.archai.chat.service;

import com.archai.chat.client.AiChatClient;
import com.archai.chat.dto.ChatConversationResponse;
import com.archai.chat.dto.ChatMessageResponse;
import com.archai.chat.dto.CreateConversationRequest;
import com.archai.chat.dto.SendMessageRequest;
import com.archai.chat.entity.ChatConversation;
import com.archai.chat.entity.ChatMessage;
import com.archai.chat.repository.ChatConversationRepository;
import com.archai.chat.repository.ChatMessageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ChatApplicationService {
    private static final Logger logger = LoggerFactory.getLogger(ChatApplicationService.class);
    private static final int CONTEXT_MESSAGES = 10;
    private static final int HISTORY_LIMIT = 200;

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final AiChatClient aiChatClient;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String notificationTopic;

    public ChatApplicationService(
        ChatConversationRepository conversationRepository,
        ChatMessageRepository messageRepository,
        AiChatClient aiChatClient,
        KafkaTemplate<String, String> kafkaTemplate,
        ObjectMapper objectMapper,
        @Value("${app.notifications.topic:archai.notifications}") String notificationTopic
    ) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.aiChatClient = aiChatClient;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.notificationTopic = notificationTopic;
    }

    public List<ChatConversationResponse> list(String ownerId) {
        return conversationRepository.findAllByOwnerIdOrderByUpdatedAtDesc(ownerId).stream()
            .map(ChatConversationResponse::from)
            .toList();
    }

    public ChatConversationResponse create(String ownerId, CreateConversationRequest request) {
        ChatConversation conversation = new ChatConversation();
        conversation.setOwnerId(ownerId);
        conversation.setTitle(request.title().trim());
        return ChatConversationResponse.from(conversationRepository.save(conversation));
    }

    public List<ChatMessageResponse> messages(Long conversationId, String ownerId) {
        requireConversation(conversationId, ownerId);
        return messageRepository.findAllByConversationIdAndOwnerIdOrderByCreatedAtAsc(
                conversationId,
                ownerId,
                PageRequest.of(0, HISTORY_LIMIT)
            ).stream()
            .map(ChatMessageResponse::from)
            .toList();
    }

    public ChatMessageResponse send(Long conversationId, String ownerId, SendMessageRequest request) {
        ChatConversation conversation = requireConversation(conversationId, ownerId);
        List<ChatMessage> recentMessages = new ArrayList<>(messageRepository
            .findAllByConversationIdAndOwnerIdOrderByCreatedAtDesc(
                conversationId,
                ownerId,
                PageRequest.of(0, CONTEXT_MESSAGES)
            ));
        java.util.Collections.reverse(recentMessages);
        String context = recentMessages.stream()
            .map(message -> message.getRole() + ": " + message.getContent())
            .reduce((left, right) -> left + "\n" + right)
            .orElse("No prior messages.");

        ChatMessage userMessage = new ChatMessage();
        userMessage.setConversationId(conversationId);
        userMessage.setOwnerId(ownerId);
        userMessage.setRole("user");
        userMessage.setContent(request.message().trim());
        messageRepository.save(userMessage);

        String reply = aiChatClient.reply(ownerId, request.message().trim(), context);
        ChatMessage assistantMessage = new ChatMessage();
        assistantMessage.setConversationId(conversationId);
        assistantMessage.setOwnerId(ownerId);
        assistantMessage.setRole("assistant");
        assistantMessage.setContent(reply);
        messageRepository.save(assistantMessage);
        conversation.setUpdatedAt(Instant.now());
        conversationRepository.save(conversation);
        publishNotification(ownerId, conversation, assistantMessage.getId());
        return ChatMessageResponse.from(assistantMessage);
    }

    private ChatConversation requireConversation(Long conversationId, String ownerId) {
        return conversationRepository.findByIdAndOwnerId(conversationId, ownerId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
    }

    private void publishNotification(String ownerId, ChatConversation conversation, Long messageId) {
        try {
            String event = objectMapper.writeValueAsString(new NotificationEvent(
                ownerId,
                "CHAT_REPLY",
                "AI response ready",
                "ArchAI replied in " + conversation.getTitle(),
                messageId
            ));
            kafkaTemplate.send(notificationTopic, ownerId, event).whenComplete((result, error) -> {
                if (error != null) logger.warn("Could not publish chat notification for message {}", messageId, error);
            });
        } catch (JsonProcessingException | RuntimeException exception) {
            logger.warn("Could not publish chat notification for message {}", messageId, exception);
        }
    }

    private record NotificationEvent(String ownerId, String type, String title, String message, Long sourceId) {}
}
