package org.example.hwtask.identity.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Регистрация пользователя")
public record RegisterRequest(
        @NotBlank @Email
        String email,

        @NotBlank @Size(min = 6, max = 128)
        String password,

        @NotBlank @Size(max = 255)
        String displayName
) {
}
