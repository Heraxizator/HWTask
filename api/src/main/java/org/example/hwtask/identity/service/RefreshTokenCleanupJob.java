package org.example.hwtask.identity.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenCleanupJob {

    private final RefreshTokenService refreshTokenService;

    public RefreshTokenCleanupJob(RefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
    }

    @Scheduled(fixedDelayString = "${hwtask.auth.refresh.cleanup-interval-ms:3600000}")
    public void cleanup() {
        refreshTokenService.cleanupExpired();
    }
}

