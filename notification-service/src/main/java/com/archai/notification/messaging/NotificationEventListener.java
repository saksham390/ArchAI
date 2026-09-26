package com.archai.notification.messaging;

import com.archai.notification.entity.AppNotification;
import com.archai.notification.repository.NotificationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class NotificationEventListener {
    private static final Logger logger = LoggerFactory.getLogger(NotificationEventListener.class);

    private final ObjectMapper objectMapper;
    private final NotificationRepository repository;

    public NotificationEventListener(ObjectMapper objectMapper, NotificationRepository repository) {
        this.objectMapper = objectMapper;
        this.repository = repository;
    }

    @KafkaListener(topics = "${app.notifications.topic:archai.notifications}", groupId = "${spring.application.name}")
    @Transactional
    public void receive(String payload) {
        try {
            NotificationEvent event = objectMapper.readValue(payload, NotificationEvent.class);
            AppNotification notification = new AppNotification();
            notification.setOwnerId(event.ownerId());
            notification.setType(event.type());
            notification.setTitle(event.title());
            notification.setMessage(event.message());
            notification.setSourceId(event.sourceId());
            notification.setRead(false);
            repository.save(notification);
        } catch (JsonProcessingException exception) {
            logger.warn("Ignoring malformed notification event");
        }
    }

    private record NotificationEvent(String ownerId, String type, String title, String message, Long sourceId) {}
}
