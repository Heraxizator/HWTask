package org.example.hwtask.reports.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProjectExtendedStatsResponse(
        TaskSummaryReportResponse tasks,
        long avgLeadTimeHours,
        List<DayCount> throughputCreated,
        List<DayCount> throughputDone,
        List<DaySeconds> timeByDaySeconds,
        List<UserSeconds> timeByUserSeconds,
        List<ActivityDayCount> activityByDay
) {
    public record DayCount(LocalDate day, long count) {}

    public record DaySeconds(LocalDate day, long seconds) {}

    public record UserSeconds(UUID userId, long seconds) {}

    public record ActivityDayCount(LocalDate day, String type, long count) {}
}

