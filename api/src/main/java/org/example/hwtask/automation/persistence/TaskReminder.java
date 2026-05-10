package org.example.hwtask.automation.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "task_reminders")
public class TaskReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "remind_at", nullable = false)
    private Instant remindAt;

    @Column(name = "fired_at")
    private Instant firedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected TaskReminder() {
    }

    public TaskReminder(UUID taskId, UUID userId, Instant remindAt) {
        this.taskId = taskId;
        this.userId = userId;
        this.remindAt = remindAt;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public UUID getUserId() {
        return userId;
    }

    public Instant getRemindAt() {
        return remindAt;
    }

    public Instant getFiredAt() {
        return firedAt;
    }

    public void setFiredAt(Instant firedAt) {
        this.firedAt = firedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
