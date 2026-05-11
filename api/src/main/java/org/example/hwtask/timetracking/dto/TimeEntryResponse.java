package org.example.hwtask.timetracking.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TimeEntryResponse(
        UUID id,
        UUID taskId,
        UUID userId,
        OffsetDateTime startedAt,
        OffsetDateTime endedAt,
        String commentNote,
        Long durationSeconds
) {
}
