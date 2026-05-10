package org.example.hwtask.identity.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT и профиль")
public record AuthResponse(
        String accessToken,
        String tokenType,
        UserPublicResponse user
) {
}
