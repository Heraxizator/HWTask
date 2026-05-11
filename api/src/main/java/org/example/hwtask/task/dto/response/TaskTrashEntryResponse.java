package org.example.hwtask.task.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TaskTrashEntryResponse(
        UUID id,
        UUID projectId,
        String title,
        OffsetDateTime deletedAt
) {
}
