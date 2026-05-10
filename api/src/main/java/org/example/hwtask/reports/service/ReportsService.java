package org.example.hwtask.reports.service;

import org.example.hwtask.identity.service.AccessControlService;
import org.example.hwtask.reports.web.dto.TaskSummaryReportResponse;
import org.example.hwtask.task.persistence.TaskRepository;
import org.example.hwtask.task.persistence.TaskStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class ReportsService {

    private final TaskRepository taskRepository;
    private final AccessControlService accessControlService;

    public ReportsService(TaskRepository taskRepository, AccessControlService accessControlService) {
        this.taskRepository = taskRepository;
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
}
