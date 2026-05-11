package org.example.hwtask.automation.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CreateReminderRequest(
        @NotNull Instant remindAt
) {
}
