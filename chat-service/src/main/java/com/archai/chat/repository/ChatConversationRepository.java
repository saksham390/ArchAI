package com.archai.chat.repository;

import com.archai.chat.entity.ChatConversation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {
    List<ChatConversation> findAllByOwnerIdOrderByUpdatedAtDesc(String ownerId);
    Optional<ChatConversation> findByIdAndOwnerId(Long id, String ownerId);
}
