package org.example.hwtask.identity.web.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        UUID organizationId,
        String name,
        OffsetDateTime createdAt
) {
}
