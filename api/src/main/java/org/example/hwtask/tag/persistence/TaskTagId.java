package org.example.hwtask.tag.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class TaskTagId implements Serializable {

    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(name = "tag_id", nullable = false)
    private UUID tagId;

    protected TaskTagId() {
    }

    public TaskTagId(UUID taskId, UUID tagId) {
        this.taskId = taskId;
        this.tagId = tagId;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public UUID getTagId() {
        return tagId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TaskTagId taskTagId = (TaskTagId) o;
        return Objects.equals(taskId, taskTagId.taskId) && Objects.equals(tagId, taskTagId.tagId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, tagId);
    }
}
