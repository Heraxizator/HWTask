package org.example.hwtask.collaboration.web.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AttachmentResponse(
        UUID id,
        UUID uploadedBy,
        String fileName,
        String contentType,
        long sizeBytes,
        OffsetDateTime createdAt
) {
}
