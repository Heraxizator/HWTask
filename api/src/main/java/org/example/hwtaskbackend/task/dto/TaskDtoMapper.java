package org.example.hwtaskbackend.task.dto;

import org.example.hwtaskbackend.task.dto.response.TaskResponse;
import org.example.hwtaskbackend.task.persistence.Task;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public final class TaskDtoMapper {

    private TaskDtoMapper() {
    }

    public static TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                toUtc(task.getCreatedAt()),
                toUtc(task.getUpdatedAt())
        );
    }

    private static OffsetDateTime toUtc(java.time.Instant instant) {
        return instant == null ? null : OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
