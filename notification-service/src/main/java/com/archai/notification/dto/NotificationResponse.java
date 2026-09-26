package com.archai.notification.dto;

import com.archai.notification.entity.AppNotification;
import java.time.Instant;

public record NotificationResponse(
    Long id,
    String type,
    String title,
    String message,
    Long sourceId,
    boolean read,
    Instant createdAt
) {
    public static NotificationResponse from(AppNotification notification) {
        return new NotificationResponse(
            notification.getId(),
            notification.getType(),
            notification.getTitle(),
            notification.getMessage(),
            notification.getSourceId(),
            notification.isRead(),
            notification.getCreatedAt()
        );
    }
}
