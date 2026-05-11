package org.example.hwtask.identity.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Профиль пользователя после успешной аутентификации")
public record AuthUserResponse(
        UserPublicResponse user
) {
}

