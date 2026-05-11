package org.example.hwtask.checklist.dto;

import jakarta.validation.constraints.Size;

public record UpdateChecklistItemRequest(
        @Size(max = 512)
        String title,
        Boolean done,
        Integer sortOrder
) {
}
