package org.example.hwtaskbackend.task.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.hwtaskbackend.task.persistence.TaskPriority;
import org.example.hwtaskbackend.task.persistence.TaskStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Task resource. All timestamps are UTC (RFC 3339).")
public record TaskResponse(
        UUID id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,

        @Schema(description = "Creation time in UTC")
        OffsetDateTime createdAt,

        @Schema(description = "Last update time in UTC")
        OffsetDateTime updatedAt
) {
}
