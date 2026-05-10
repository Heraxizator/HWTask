package org.example.hwtaskbackend.task.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.example.hwtaskbackend.task.persistence.TaskPriority;
import org.example.hwtaskbackend.task.persistence.TaskStatus;

@Schema(description = "Create task body. Timestamps in responses are UTC (RFC 3339).")
public record CreateTaskRequest(
        @NotBlank
        @Size(max = 255)
        String title,

        @Size(max = 10000)
        String description,

        TaskStatus status,

        TaskPriority priority
) {
}
