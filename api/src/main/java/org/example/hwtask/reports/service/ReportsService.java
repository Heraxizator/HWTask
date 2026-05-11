package org.example.hwtask.reports.service;

import org.example.hwtask.identity.service.AccessControlService;
import org.example.hwtask.collaboration.persistence.TaskActivityRepository;
import org.example.hwtask.reports.dto.ProjectExtendedStatsResponse;
import org.example.hwtask.reports.dto.ProjectTimeSummaryResponse;
import org.example.hwtask.reports.dto.TaskSummaryReportResponse;
import org.example.hwtask.task.persistence.TaskRepository;
import org.example.hwtask.timetracking.persistence.TaskTimeEntryRepository;
import org.example.hwtask.task.persistence.TaskStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class ReportsService {

    private final TaskRepository taskRepository;
    private final TaskTimeEntryRepository taskTimeEntryRepository;
    private final TaskActivityRepository taskActivityRepository;
    private final AccessControlService accessControlService;

    public ReportsService(
            TaskRepository taskRepository,
            TaskTimeEntryRepository taskTimeEntryRepository,
            TaskActivityRepository taskActivityRepository,
            AccessControlService accessControlService
    ) {
        this.taskRepository = taskRepository;
        this.taskTimeEntryRepository = taskTimeEntryRepository;
        this.taskActivityRepository = taskActivityRepository;
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

    @Transactional(readOnly = true)
    public ProjectExtendedStatsResponse extended(UUID projectId, UUID actorUserId, Instant from, Instant to) {
        accessControlService.assertProjectMember(projectId, actorUserId);

        TaskSummaryReportResponse tasks = taskSummary(projectId, actorUserId);

        long leadHours = Math.round(taskRepository.avgLeadTimeHoursForDoneTasks(projectId, from, to));

        List<ProjectExtendedStatsResponse.DayCount> created = taskRepository.countCreatedByDay(projectId, from, to).stream()
                .map(r -> new ProjectExtendedStatsResponse.DayCount(
                        ((java.sql.Date) r[0]).toLocalDate(),
                        ((Number) r[1]).longValue()
                ))
                .toList();

        List<ProjectExtendedStatsResponse.DayCount> done = taskRepository.countDoneByDay(projectId, from, to).stream()
                .map(r -> new ProjectExtendedStatsResponse.DayCount(
                        ((java.sql.Date) r[0]).toLocalDate(),
                        ((Number) r[1]).longValue()
                ))
                .toList();

        List<ProjectExtendedStatsResponse.DaySeconds> timeByDay = taskTimeEntryRepository.sumSecondsByDayForProject(projectId, from, to).stream()
                .map(r -> new ProjectExtendedStatsResponse.DaySeconds(
                        ((java.sql.Date) r[0]).toLocalDate(),
                        ((Number) r[1]).longValue()
                ))
                .toList();

        List<ProjectExtendedStatsResponse.UserSeconds> timeByUser = taskTimeEntryRepository.sumSecondsByUserForProject(projectId).stream()
                .map(r -> new ProjectExtendedStatsResponse.UserSeconds((UUID) r[0], ((Number) r[1]).longValue()))
                .toList();

        List<ProjectExtendedStatsResponse.ActivityDayCount> activity = taskActivityRepository.countByDayAndTypeForProject(projectId, from, to).stream()
                .map(r -> new ProjectExtendedStatsResponse.ActivityDayCount(
                        ((java.sql.Date) r[0]).toLocalDate(),
                        String.valueOf(r[1]),
                        ((Number) r[2]).longValue()
                ))
                .toList();

        return new ProjectExtendedStatsResponse(
                tasks,
                leadHours,
                created,
                done,
                timeByDay,
                timeByUser,
                activity
        );
    }
}
