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

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }
        User user = new User(email, passwordEncoder.encode(request.password()), request.displayName().trim());
        userRepository.save(user);
        return toAuth(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Неверный email или пароль"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Неверный email или пароль");
        }
        return toAuth(user);
    }

    private AuthResponse toAuth(User user) {
        String token = jwtService.createToken(user);
        UserPublicResponse u = new UserPublicResponse(user.getId(), user.getEmail(), user.getDisplayName());
        return new AuthResponse(token, "Bearer", u);
    }
}
