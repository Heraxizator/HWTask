package org.example.hwtask.identity.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Публичные данные пользователя")
public record UserPublicResponse(
        UUID id,
        String email,
        String displayName
) {
}
