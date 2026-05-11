package org.example.hwtask.checklist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateChecklistItemRequest(
        @NotBlank
        @Size(max = 512)
        String title
) {
}
