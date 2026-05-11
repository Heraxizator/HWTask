package org.example.hwtask.reports.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.hwtask.reports.dto.ProjectExtendedStatsResponse;
import org.example.hwtask.reports.service.ReportsService;
import org.example.hwtask.reports.dto.ProjectTimeSummaryResponse;
import org.example.hwtask.reports.dto.TaskSummaryReportResponse;
import org.example.hwtask.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/reports")
@Tag(name = "Reports")
public class ReportsController {

    private final ReportsService reportsService;

    public ReportsController(ReportsService reportsService) {
        this.reportsService = reportsService;
    }

    @GetMapping("/tasks-summary")
    @Operation(summary = "Сводка по задачам проекта")
    public TaskSummaryReportResponse taskSummary(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID projectId
    ) {
        return reportsService.taskSummary(projectId, user.getId());
    }

    @GetMapping("/time-summary")
    @Operation(summary = "Сводка учёта времени по проекту")
    public ProjectTimeSummaryResponse timeSummary(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID projectId
    ) {
        return reportsService.projectTimeSummary(projectId, user.getId());
    }

    @GetMapping("/extended")
    @Operation(summary = "Расширенная статистика проекта (v1)")
    public ProjectExtendedStatsResponse extended(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID projectId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        Instant fromI = parseDateOrDefault(from, LocalDate.now(ZoneOffset.UTC).minusDays(30)).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant toI = parseDateOrDefault(to, LocalDate.now(ZoneOffset.UTC).plusDays(1)).atStartOfDay().toInstant(ZoneOffset.UTC);
        return reportsService.extended(projectId, user.getId(), fromI, toI);
    }

    private static LocalDate parseDateOrDefault(String v, LocalDate def) {
        if (v == null || v.isBlank()) return def;
        return LocalDate.parse(v.trim());
    }
}
