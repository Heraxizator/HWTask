package org.example.hwtask.task.dto.response;

import org.example.hwtask.task.persistence.TaskMemberRole;

import java.util.UUID;

public record TaskMemberEntryResponse(
        UUID userId,
        TaskMemberRole role
) {
}
