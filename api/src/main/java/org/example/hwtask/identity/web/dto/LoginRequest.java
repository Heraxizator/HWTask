package org.example.hwtask.identity.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Вход")
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {
}
