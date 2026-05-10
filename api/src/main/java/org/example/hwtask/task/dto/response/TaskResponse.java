package org.example.hwtask.task.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.hwtask.task.persistence.TaskPriority;
import org.example.hwtask.task.persistence.TaskStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Задача")
public record TaskResponse(
        UUID id,
        UUID projectId,
        UUID parentTaskId,
        UUID assigneeId,
        UUID createdBy,
        OffsetDateTime dueAt,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        @Schema(description = "UTC")
        OffsetDateTime createdAt,
        @Schema(description = "UTC")
        OffsetDateTime updatedAt,
        List<TaskMemberEntryResponse> extraMembers
) {
}
