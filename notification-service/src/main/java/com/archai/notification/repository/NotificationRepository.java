package com.archai.notification.repository;

import com.archai.notification.entity.AppNotification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<AppNotification, Long> {
    List<AppNotification> findAllByOwnerIdOrderByCreatedAtDesc(String ownerId, Pageable pageable);
    Optional<AppNotification> findByIdAndOwnerId(Long id, String ownerId);
}
