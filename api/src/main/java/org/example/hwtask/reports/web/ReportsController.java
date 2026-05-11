package org.example.hwtask.reports.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.hwtask.reports.service.ReportsService;
import org.example.hwtask.reports.dto.TaskSummaryReportResponse;
import org.example.hwtask.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
