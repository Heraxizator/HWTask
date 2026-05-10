package org.example.hwtask.task.persistence;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "task_members")
public class TaskMember {

    @EmbeddedId
    private TaskMemberId id;

    protected TaskMember() {
    }

    public TaskMember(UUID taskId, UUID userId, TaskMemberRole role) {
        this.id = new TaskMemberId(taskId, userId, role);
    }

    public TaskMemberId getId() {
        return id;
    }
}
