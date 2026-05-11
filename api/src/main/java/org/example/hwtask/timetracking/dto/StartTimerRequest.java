package org.example.hwtask.timetracking.dto;

import jakarta.validation.constraints.Size;

public record StartTimerRequest(
        @Size(max = 2000)
        String commentNote
) {
}
