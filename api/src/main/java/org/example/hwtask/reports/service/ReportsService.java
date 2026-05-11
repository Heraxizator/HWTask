package org.example.hwtask.reports.service;

import org.example.hwtask.identity.service.AccessControlService;
import org.example.hwtask.reports.dto.ProjectTimeSummaryResponse;
import org.example.hwtask.reports.dto.TaskSummaryReportResponse;
import org.example.hwtask.task.persistence.TaskRepository;
import org.example.hwtask.timetracking.persistence.TaskTimeEntryRepository;
import org.example.hwtask.task.persistence.TaskStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ReportsService {

    private final TaskRepository taskRepository;
    private final TaskTimeEntryRepository taskTimeEntryRepository;
    private final AccessControlService accessControlService;

    public ReportsService(
            TaskRepository taskRepository,
            TaskTimeEntryRepository taskTimeEntryRepository,
            AccessControlService accessControlService
    ) {
        this.taskRepository = taskRepository;
        this.taskTimeEntryRepository = taskTimeEntryRepository;
        this.accessControlService = accessControlService;
    }

    @Transactional(readOnly = true)
    public TaskSummaryReportResponse taskSummary(UUID projectId, UUID actorUserId) {
        accessControlService.assertProjectMember(projectId, actorUserId);
        Instant now = Instant.now();
        long todo = taskRepository.countByProjectIdAndStatus(projectId, TaskStatus.TODO);
        long inProgress = taskRepository.countByProjectIdAndStatus(projectId, TaskStatus.IN_PROGRESS);
        long done = taskRepository.countByProjectIdAndStatus(projectId, TaskStatus.DONE);
        long overdue = taskRepository.countOverdue(projectId, now, TaskStatus.DONE);
        long total = taskRepository.countByProjectId(projectId);
        return new TaskSummaryReportResponse(total, todo, inProgress, done, overdue);
    }

    @Transactional(readOnly = true)
    public ProjectTimeSummaryResponse projectTimeSummary(UUID projectId, UUID actorUserId) {
        accessControlService.assertProjectMember(projectId, actorUserId);
        double total = taskTimeEntryRepository.sumSecondsForProject(projectId);
        List<Object[]> rows = taskTimeEntryRepository.sumSecondsByUserForProject(projectId);
        List<ProjectTimeSummaryResponse.UserTimeShareResponse> byUser = rows.stream()
                .map(r -> new ProjectTimeSummaryResponse.UserTimeShareResponse(
                        (UUID) r[0],
                        ((Number) r[1]).longValue()
                ))
                .toList();
        return new ProjectTimeSummaryResponse((long) total, byUser);
    }
}
