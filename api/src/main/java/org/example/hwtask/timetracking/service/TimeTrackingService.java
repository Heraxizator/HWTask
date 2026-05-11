package org.example.hwtask.timetracking.service;

import org.example.hwtask.identity.service.AccessControlService;
import org.example.hwtask.task.persistence.Task;
import org.example.hwtask.task.persistence.TaskRepository;
import org.example.hwtask.task.service.TaskNotFoundException;
import org.example.hwtask.timetracking.dto.TimeEntryResponse;
import org.example.hwtask.timetracking.persistence.TaskTimeEntry;
import org.example.hwtask.timetracking.persistence.TaskTimeEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class TimeTrackingService {

    private final TaskTimeEntryRepository entryRepository;
    private final TaskRepository taskRepository;
    private final AccessControlService accessControlService;

    public TimeTrackingService(
            TaskTimeEntryRepository entryRepository,
            TaskRepository taskRepository,
            AccessControlService accessControlService
    ) {
        this.entryRepository = entryRepository;
        this.taskRepository = taskRepository;
        this.accessControlService = accessControlService;
    }

    @Transactional
    public TimeEntryResponse startTimer(UUID taskId, UUID userId, String commentNote) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));
        accessControlService.assertProjectMember(task.getProjectId(), userId);
        Instant now = Instant.now();
        entryRepository.findFirstByUserIdAndEndedAtIsNullOrderByStartedAtDesc(userId).ifPresent(open -> {
            open.setEndedAt(now);
            entryRepository.save(open);
        });
        TaskTimeEntry e = new TaskTimeEntry(taskId, userId, now, commentNote);
        TaskTimeEntry saved = entryRepository.save(e);
        return toResponse(saved);
    }

    @Transactional
    public TimeEntryResponse stopTimer(UUID userId) {
        TaskTimeEntry open = entryRepository.findFirstByUserIdAndEndedAtIsNullOrderByStartedAtDesc(userId)
                .orElseThrow(() -> new IllegalArgumentException("Нет активного учёта времени"));
        open.setEndedAt(Instant.now());
        entryRepository.save(open);
        return toResponse(open);
    }

    @Transactional(readOnly = true)
    public List<TimeEntryResponse> listTaskEntries(UUID taskId, UUID actorUserId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));
        accessControlService.assertProjectMember(task.getProjectId(), actorUserId);
        return entryRepository.findByTaskIdOrderByStartedAtDesc(taskId).stream()
                .map(this::toResponse)
                .toList();
    }

    private TimeEntryResponse toResponse(TaskTimeEntry e) {
        Long dur = null;
        if (e.getEndedAt() != null) {
            dur = ChronoUnit.SECONDS.between(e.getStartedAt(), e.getEndedAt());
        }
        return new TimeEntryResponse(
                e.getId(),
                e.getTaskId(),
                e.getUserId(),
                OffsetDateTime.ofInstant(e.getStartedAt(), ZoneOffset.UTC),
                e.getEndedAt() == null ? null : OffsetDateTime.ofInstant(e.getEndedAt(), ZoneOffset.UTC),
                e.getCommentNote(),
                dur
        );
    }
}
