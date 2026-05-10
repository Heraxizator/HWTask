package org.example.hwtask.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "hwtask.jwt")
@Validated
public record JwtProperties(
        @NotBlank(message = "JWT secret is required (set hwtask.jwt.secret or JWT_SECRET / HWTASK_JWT_SECRET for docker/prod)")
        String secret,
        @Positive(message = "JWT expiration must be positive (hwtask.jwt.expiration-ms / JWT_EXPIRATION_MS)")
        long expirationMs
) {
}
