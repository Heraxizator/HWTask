package org.example.hwtask.identity.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.hwtask.identity.service.AuthService;
import org.example.hwtask.identity.service.AuthCookieProperties;
import org.example.hwtask.identity.service.RefreshTokenService;
import org.example.hwtask.identity.service.UnauthorizedException;
import org.example.hwtask.identity.web.dto.AuthResponse;
import org.example.hwtask.identity.web.dto.LoginRequest;
import org.example.hwtask.identity.web.dto.RegisterRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth")
public class AuthController {

    private final AuthService authService;
    private final AuthCookieProperties cookieProperties;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthService authService, AuthCookieProperties cookieProperties, RefreshTokenService refreshTokenService) {
        this.authService = authService;
        this.cookieProperties = cookieProperties;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Регистрация")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthService.AuthSession session = authService.registerSession(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, accessCookie(session.accessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie(session.refreshToken()).toString())
                .body(new AuthResponse(session.accessToken(), "Bearer", session.user()));
    }

    @PostMapping("/login")
    @Operation(summary = "Вход")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthService.AuthSession session = authService.loginSession(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie(session.accessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie(session.refreshToken()).toString())
                .body(new AuthResponse(session.accessToken(), "Bearer", session.user()));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Обновить сессию (refresh)")
    public ResponseEntity<Void> refresh(HttpServletRequest request) {
        String rawRefresh = readCookie(request, cookieProperties.refreshName());
        var principal = refreshTokenService.validate(rawRefresh)
                .orElseThrow(() -> new UnauthorizedException("Refresh token is invalid"));

        refreshTokenService.revoke(rawRefresh);
        AuthService.AuthSession session = authService.issueSession(principal.user());
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, accessCookie(session.accessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie(session.refreshToken()).toString())
                .build();
    }

    @PostMapping("/logout")
    @Operation(summary = "Выход (logout)")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String rawRefresh = readCookie(request, cookieProperties.refreshName());
        refreshTokenService.revoke(rawRefresh);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, clearCookie(cookieProperties.accessName(), "/").toString())
                .header(HttpHeaders.SET_COOKIE, clearCookie(cookieProperties.refreshName(), "/api/v1/auth").toString())
                .build();
    }

    @GetMapping("/csrf")
    @Operation(summary = "Инициализация CSRF cookie для SPA")
    public ResponseEntity<Void> csrf() {
        return ResponseEntity.noContent().build();
    }

    private ResponseCookie accessCookie(String value) {
        return baseCookie(cookieProperties.accessName(), value)
                .path("/")
                .maxAge(cookieProperties.accessTtlSeconds())
                .build();
    }

    private ResponseCookie refreshCookie(String value) {
        return baseCookie(cookieProperties.refreshName(), value)
                .path("/api/v1/auth")
                .maxAge(cookieProperties.refreshTtlSeconds())
                .build();
    }

    private ResponseCookie clearCookie(String name, String path) {
        return baseCookie(name, "")
                .path(path)
                .maxAge(0)
                .build();
    }

    private ResponseCookie.ResponseCookieBuilder baseCookie(String name, String value) {
        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieProperties.secure())
                .sameSite(cookieProperties.sameSite());
        if (cookieProperties.domain() != null && !cookieProperties.domain().isBlank()) {
            b.domain(cookieProperties.domain().trim());
        }
        return b;
    }

    private static String readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }
}
