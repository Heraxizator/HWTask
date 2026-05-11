package org.example.hwtask.tag.persistence;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "task_tags")
public class TaskTag {

    @EmbeddedId
    private TaskTagId id;

    protected TaskTag() {
    }

    public TaskTag(UUID taskId, UUID tagId) {
        this.id = new TaskTagId(taskId, tagId);
    }

    public TaskTagId getId() {
        return id;
    }
}
