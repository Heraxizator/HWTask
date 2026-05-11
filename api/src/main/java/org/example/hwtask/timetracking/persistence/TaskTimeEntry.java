package org.example.hwtask.timetracking.persistence;

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
@Table(name = "task_time_entries")
public class TaskTimeEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "comment_note", length = 2000)
    private String commentNote;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected TaskTimeEntry() {
    }

    public TaskTimeEntry(UUID taskId, UUID userId, Instant startedAt, String commentNote) {
        this.taskId = taskId;
        this.userId = userId;
        this.startedAt = startedAt;
        this.commentNote = commentNote;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
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

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Instant endedAt) {
        this.endedAt = endedAt;
    }

    public String getCommentNote() {
        return commentNote;
    }

    public void setCommentNote(String commentNote) {
        this.commentNote = commentNote;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
