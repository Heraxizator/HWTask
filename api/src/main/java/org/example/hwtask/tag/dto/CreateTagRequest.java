package org.example.hwtask.tag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTagRequest(
        @NotBlank
        @Size(max = 128)
        String name
) {
}
