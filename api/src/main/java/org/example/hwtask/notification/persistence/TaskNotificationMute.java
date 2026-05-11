package org.example.hwtask.notification.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "task_notification_mutes")
public class TaskNotificationMute {

    @EmbeddedId
    private TaskNotificationMuteId id;

    protected TaskNotificationMute() {
    }

    public TaskNotificationMute(UUID taskId, UUID userId) {
        this.id = new TaskNotificationMuteId(taskId, userId);
    }

    public TaskNotificationMuteId getId() {
        return id;
    }
}
