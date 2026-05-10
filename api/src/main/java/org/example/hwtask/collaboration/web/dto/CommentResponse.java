package org.example.hwtask.collaboration.web.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID authorId,
        String body,
        OffsetDateTime createdAt
) {
}
