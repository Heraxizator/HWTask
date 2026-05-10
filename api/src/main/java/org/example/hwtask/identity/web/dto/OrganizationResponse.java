package org.example.hwtask.identity.web.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OrganizationResponse(
        UUID id,
        String name,
        OffsetDateTime createdAt
) {
}
