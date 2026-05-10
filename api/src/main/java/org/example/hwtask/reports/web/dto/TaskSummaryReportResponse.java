package org.example.hwtask.reports.web.dto;

public record TaskSummaryReportResponse(
        long total,
        long todo,
        long inProgress,
        long done,
        long overdue
) {
}
