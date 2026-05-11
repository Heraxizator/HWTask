package org.example.hwtask.identity.service;

import org.example.hwtask.identity.persistence.User;
import org.example.hwtask.identity.persistence.UserRepository;
import org.example.hwtask.identity.web.dto.AuthResponse;
import org.example.hwtask.identity.web.dto.LoginRequest;
import org.example.hwtask.identity.web.dto.RegisterRequest;
import org.example.hwtask.identity.web.dto.UserPublicResponse;
import org.example.hwtask.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthCookieProperties cookieProperties;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthCookieProperties cookieProperties,
            RefreshTokenService refreshTokenService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.cookieProperties = cookieProperties;
        this.refreshTokenService = refreshTokenService;
    }

    public record AuthSession(String accessToken, String refreshToken, UserPublicResponse user) {
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        AuthSession session = registerSession(request);
        return new AuthResponse(session.accessToken(), "Bearer", session.user());
    }

    @Transactional
    public AuthSession registerSession(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }
        User user = new User(email, passwordEncoder.encode(request.password()), request.displayName().trim());
        userRepository.save(user);
        return toSession(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        AuthSession session = loginSession(request);
        return new AuthResponse(session.accessToken(), "Bearer", session.user());
    }

    @Transactional(readOnly = true)
    public AuthSession loginSession(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Неверный email или пароль"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Неверный email или пароль");
        }
        return toSession(user);
    }

    @Transactional
    public AuthSession issueSession(User user) {
        return toSession(user);
    }

    private AuthSession toSession(User user) {
        String access = jwtService.createToken(user, cookieProperties.accessTtlSeconds() * 1000);
        var refresh = refreshTokenService.issue(user, cookieProperties.refreshTtlSeconds());
        UserPublicResponse u = new UserPublicResponse(user.getId(), user.getEmail(), user.getDisplayName());
        return new AuthSession(access, refresh.rawToken(), u);
    }
}
