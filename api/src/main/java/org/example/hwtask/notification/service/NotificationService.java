package org.example.hwtask.notification.service;

import org.example.hwtask.notification.dto.NotificationResponse;
import org.example.hwtask.notification.persistence.Notification;
import org.example.hwtask.notification.persistence.NotificationRepository;
import org.example.hwtask.notification.persistence.NotificationType;
import org.example.hwtask.notification.persistence.TaskNotificationMute;
import org.example.hwtask.notification.persistence.TaskNotificationMuteRepository;
import org.example.hwtask.task.persistence.Task;
import org.example.hwtask.task.persistence.TaskMember;
import org.example.hwtask.task.persistence.TaskMemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final TaskNotificationMuteRepository muteRepository;
    private final TaskMemberRepository taskMemberRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            TaskNotificationMuteRepository muteRepository,
            TaskMemberRepository taskMemberRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.muteRepository = muteRepository;
        this.taskMemberRepository = taskMemberRepository;
    }

    /**
     * In-app notifications for assignee, author, co-assignees and observers (excluding actor); skips muted users.
     */
    @Transactional
    public void notifyTaskAudience(Task task, NotificationType type, UUID actorUserId, String title, String body) {
        Set<UUID> recipients = new LinkedHashSet<>();
        if (task.getAssigneeId() != null) {
            recipients.add(task.getAssigneeId());
        }
        if (task.getCreatedBy() != null) {
            recipients.add(task.getCreatedBy());
        }
        for (TaskMember m : taskMemberRepository.findByIdTaskId(task.getId())) {
            recipients.add(m.getId().getUserId());
        }
        recipients.remove(actorUserId);
        for (UUID uid : recipients) {
            if (muteRepository.existsByIdTaskIdAndIdUserId(task.getId(), uid)) {
                continue;
            }
            notificationRepository.save(new Notification(uid, task.getId(), type, title, body));
        }
    }

    @Transactional
    public void notifySingleUser(UUID userId, UUID taskId, NotificationType type, String title, String body) {
        if (taskId != null && muteRepository.existsByIdTaskIdAndIdUserId(taskId, userId)) {
            return;
        }
        notificationRepository.save(new Notification(userId, taskId, type, title, body));
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> list(UUID userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public long unreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndReadAtIsNull(userId);
    }

    @Transactional
    public void markRead(UUID userId, UUID notificationId) {
        Notification n = notificationRepository.findById(notificationId).orElseThrow();
        if (!n.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Нет доступа к уведомлению");
        }
        n.setReadAt(Instant.now());
        notificationRepository.save(n);
    }

    @Transactional
    public void markAllRead(UUID userId) {
        notificationRepository.markAllRead(userId, Instant.now());
    }

    @Transactional
    public void muteTask(UUID taskId, UUID userId) {
        TaskNotificationMute m = new TaskNotificationMute(taskId, userId);
        muteRepository.save(m);
    }

    @Transactional
    public void unmuteTask(UUID taskId, UUID userId) {
        muteRepository.deleteByIdTaskIdAndIdUserId(taskId, userId);
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getTaskId(),
                n.getType(),
                n.getTitle(),
                n.getBody(),
                n.getReadAt() != null,
                OffsetDateTime.ofInstant(n.getCreatedAt(), ZoneOffset.UTC)
        );
    }
}
