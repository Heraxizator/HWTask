package org.example.hwtask.task.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.hwtask.task.persistence.TaskPriority;
import org.example.hwtask.task.persistence.TaskStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Создание задачи в проекте")
public record CreateTaskRequest(
        @NotNull
        UUID projectId,

        UUID parentTaskId,

        UUID assigneeId,

        Instant dueAt,

        List<UUID> coAssigneeIds,

        List<UUID> observerIds,

        @NotBlank
        @Size(max = 255)
        String title,

        @Size(max = 10000)
        String description,

        TaskStatus status,

        TaskPriority priority,

        List<UUID> tagIds
) {
    public CreateTaskRequest {
        if (coAssigneeIds == null) {
            coAssigneeIds = List.of();
        }
        if (observerIds == null) {
            observerIds = List.of();
        }
        if (tagIds == null) {
            tagIds = List.of();
        }
    }
}
