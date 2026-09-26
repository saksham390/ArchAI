package com.archai.notification.controller;

import com.archai.notification.dto.NotificationResponse;
import com.archai.notification.repository.NotificationRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationRepository repository;

    public NotificationController(NotificationRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<NotificationResponse> list(@RequestHeader("X-User-Id") String ownerId) {
        return repository.findAllByOwnerIdOrderByCreatedAtDesc(ownerId, PageRequest.of(0, 100)).stream()
            .map(NotificationResponse::from)
            .toList();
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markRead(
        @PathVariable Long id,
        @RequestHeader("X-User-Id") String ownerId
    ) {
        return repository.findByIdAndOwnerId(id, ownerId)
            .map(notification -> {
                notification.setRead(true);
                return ResponseEntity.ok(NotificationResponse.from(repository.save(notification)));
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
