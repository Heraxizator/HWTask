package org.example.hwtask.reports.dto;

import java.util.List;
import java.util.UUID;

public record ProjectTimeSummaryResponse(
        long totalSeconds,
        List<UserTimeShareResponse> byUser
) {
    public record UserTimeShareResponse(UUID userId, long seconds) {
    }
}
