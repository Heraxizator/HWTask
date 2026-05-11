package org.example.hwtask.notification.dto;

import org.example.hwtask.notification.persistence.NotificationType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID taskId,
        NotificationType type,
        String title,
        String body,
        boolean read,
        OffsetDateTime createdAt
) {
}
