package org.example.hwtask.automation.web.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReminderResponse(
        UUID id,
        UUID taskId,
        UUID userId,
        OffsetDateTime remindAt,
        OffsetDateTime firedAt
) {
}
