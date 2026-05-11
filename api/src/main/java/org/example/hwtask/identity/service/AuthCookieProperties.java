package org.example.hwtask.identity.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "hwtask.auth.cookie")
@Validated
public record AuthCookieProperties(
        @NotBlank String accessName,
        @NotBlank String refreshName,
        @Positive long accessTtlSeconds,
        @Positive long refreshTtlSeconds,
        boolean secure,
        @NotBlank String sameSite,
        String domain
) {
}

