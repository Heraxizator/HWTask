package org.example.hwtask.task.dto;

import org.example.hwtask.task.dto.response.TaskResponse;
import org.example.hwtask.task.persistence.Task;
import org.example.hwtask.task.persistence.TaskPriority;
import org.example.hwtask.task.persistence.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TaskDtoMapperTest {

    @Test
    void mapsEntityFieldsToResponseInUtc() {
        UUID id = UUID.randomUUID();
        Instant created = Instant.parse("2025-06-01T12:00:00Z");
        Instant updated = Instant.parse("2025-06-02T15:30:00Z");

        Task task = mock(Task.class);
        when(task.getId()).thenReturn(id);
        when(task.getTitle()).thenReturn("T");
        when(task.getDescription()).thenReturn("D");
        when(task.getStatus()).thenReturn(TaskStatus.IN_PROGRESS);
        when(task.getPriority()).thenReturn(TaskPriority.HIGH);
        when(task.getCreatedAt()).thenReturn(created);
        when(task.getUpdatedAt()).thenReturn(updated);
        when(task.getProjectId()).thenReturn(UUID.randomUUID());
        when(task.getParentTaskId()).thenReturn(null);
        when(task.getAssigneeId()).thenReturn(null);
        when(task.getCreatedBy()).thenReturn(UUID.randomUUID());
        when(task.getDueAt()).thenReturn(null);

        TaskResponse response = TaskDtoMapper.toResponse(task);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.title()).isEqualTo("T");
        assertThat(response.description()).isEqualTo("D");
        assertThat(response.status()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(response.priority()).isEqualTo(TaskPriority.HIGH);
        assertThat(response.createdAt().toInstant()).isEqualTo(created);
        assertThat(response.updatedAt().toInstant()).isEqualTo(updated);
    }
}
