package org.example.hwtask.identity.service;

import org.example.hwtask.identity.persistence.RefreshToken;
import org.example.hwtask.identity.persistence.RefreshTokenRepository;
import org.example.hwtask.identity.persistence.User;
import org.example.hwtask.identity.persistence.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class RefreshTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final HexFormat HEX = HexFormat.of();

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    public record IssuedRefreshToken(String rawToken, Instant expiresAt) {
    }

    public record RefreshPrincipal(User user) {
    }

    @Transactional
    public IssuedRefreshToken issue(User user, long ttlSeconds) {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        String raw = HEX.formatHex(bytes);
        String hash = sha256Hex(raw);
        Instant exp = Instant.now().plusSeconds(ttlSeconds);
        refreshTokenRepository.save(new RefreshToken(user, hash, exp));
        return new IssuedRefreshToken(raw, exp);
    }

    @Transactional(readOnly = true)
    public Optional<RefreshPrincipal> validate(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) return Optional.empty();
        String hash = sha256Hex(rawToken.trim());
        return refreshTokenRepository.findByTokenHash(hash)
                .filter(t -> !t.isRevoked())
                .filter(t -> t.getExpiresAt().isAfter(Instant.now()))
                .map(t -> new RefreshPrincipal(t.getUser()));
    }

    @Transactional
    public void revoke(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) return;
        String hash = sha256Hex(rawToken.trim());
        refreshTokenRepository.findByTokenHash(hash).ifPresent(t -> {
            if (!t.isRevoked()) {
                t.revoke();
            }
        });
    }

    @Transactional
    public long cleanupExpired() {
        return refreshTokenRepository.deleteByExpiresAtBefore(Instant.now().minusSeconds(60));
    }

    private static String sha256Hex(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(s.getBytes(StandardCharsets.UTF_8));
            return HEX.formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}

