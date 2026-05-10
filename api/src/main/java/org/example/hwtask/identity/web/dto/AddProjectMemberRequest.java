package org.example.hwtask.identity.web.dto;

import jakarta.validation.constraints.NotNull;
import org.example.hwtask.identity.persistence.ProjectRole;

import java.util.UUID;

public record AddProjectMemberRequest(
        @NotNull UUID userId,
        @NotNull ProjectRole role
) {
}
