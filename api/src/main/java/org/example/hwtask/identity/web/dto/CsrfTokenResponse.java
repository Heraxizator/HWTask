package org.example.hwtask.identity.web.dto;

public record CsrfTokenResponse(
        String headerName,
        String parameterName,
        String token
) {
}

