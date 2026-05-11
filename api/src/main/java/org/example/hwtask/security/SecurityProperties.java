package org.example.hwtask.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hwtask.security")
public record SecurityProperties(
        boolean allowBearerHeader
) {
}

