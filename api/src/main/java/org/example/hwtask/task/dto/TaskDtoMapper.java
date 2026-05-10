package org.example.hwtask.task.dto;

import org.example.hwtask.task.dto.response.TaskMemberEntryResponse;
import org.example.hwtask.task.dto.response.TaskResponse;
import org.example.hwtask.task.persistence.Task;
import org.example.hwtask.task.persistence.TaskMember;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

public final class TaskDtoMapper {

    private TaskDtoMapper() {
    }

    public static TaskResponse toResponse(Task task) {
        return toResponse(task, List.of());
    }

    public static TaskResponse toResponse(Task task, List<TaskMember> extraMembers) {
        return new TaskResponse(
                task.getId(),
                task.getProjectId(),
                task.getParentTaskId(),
                task.getAssigneeId(),
                task.getCreatedBy(),
                toUtc(task.getDueAt()),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                toUtc(task.getCreatedAt()),
                toUtc(task.getUpdatedAt()),
                extraMembers.stream()
                        .map(m -> new TaskMemberEntryResponse(m.getId().getUserId(), m.getId().getRole()))
                        .toList()
        );
    }

    private static OffsetDateTime toUtc(java.time.Instant instant) {
        return instant == null ? null : OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
