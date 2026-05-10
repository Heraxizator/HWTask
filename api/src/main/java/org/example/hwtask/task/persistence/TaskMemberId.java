package org.example.hwtask.task.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class TaskMemberId implements Serializable {

    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TaskMemberRole role;

    protected TaskMemberId() {
    }

    public TaskMemberId(UUID taskId, UUID userId, TaskMemberRole role) {
        this.taskId = taskId;
        this.userId = userId;
        this.role = role;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public UUID getUserId() {
        return userId;
    }

    public TaskMemberRole getRole() {
        return role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TaskMemberId that = (TaskMemberId) o;
        return Objects.equals(taskId, that.taskId)
                && Objects.equals(userId, that.userId)
                && role == that.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, userId, role);
    }
}
