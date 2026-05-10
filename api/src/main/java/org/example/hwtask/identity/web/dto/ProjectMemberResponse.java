package org.example.hwtask.identity.web.dto;

import org.example.hwtask.identity.persistence.ProjectRole;

import java.util.UUID;

public record ProjectMemberResponse(
        UUID userId,
        String email,
        String displayName,
        ProjectRole role
) {
}
