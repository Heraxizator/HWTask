package org.example.hwtask.collaboration.web.dto;

import org.example.hwtask.collaboration.persistence.TaskActivityType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ActivityEntryResponse(
        UUID id,
        UUID actorId,
        TaskActivityType eventType,
        String summary,
        OffsetDateTime createdAt
) {
}
