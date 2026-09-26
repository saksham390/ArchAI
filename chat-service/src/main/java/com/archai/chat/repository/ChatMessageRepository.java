package com.archai.chat.repository;

import com.archai.chat.entity.ChatMessage;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findAllByConversationIdAndOwnerIdOrderByCreatedAtAsc(Long conversationId, String ownerId, Pageable pageable);
    List<ChatMessage> findAllByConversationIdAndOwnerIdOrderByCreatedAtDesc(Long conversationId, String ownerId, Pageable pageable);
}
