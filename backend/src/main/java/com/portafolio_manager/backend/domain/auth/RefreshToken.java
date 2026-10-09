package com.portafolio_manager.backend.domain.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public record RefreshToken(
        Long id,
        Long userId,
        String tokenHash,
        Instant expiresAt,
        Instant revokedAt
) {

    public RefreshToken {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(tokenHash, "tokenHash must not be null");
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");
    }

    public static RefreshToken issue(Long userId, String tokenHash, Instant now, Duration timeToLive) {
        return new RefreshToken(null, userId, tokenHash, now.plus(timeToLive), null);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }
}
