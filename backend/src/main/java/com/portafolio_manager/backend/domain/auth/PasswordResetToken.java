package com.portafolio_manager.backend.domain.auth;

import java.time.Instant;
import java.util.Objects;

/**
 * A password recovery link (HU010). Only the hash of the token is kept; it can be used once,
 * before it expires.
 */
public record PasswordResetToken(Long id, Long userId, String tokenHash, Instant expiresAt, Instant usedAt) {

    public PasswordResetToken {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(tokenHash, "tokenHash must not be null");
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");
    }

    public static PasswordResetToken issue(Long userId, String tokenHash, Instant expiresAt) {
        return new PasswordResetToken(null, userId, tokenHash, expiresAt, null);
    }

    public boolean isUsableAt(Instant now) {
        return usedAt == null && now.isBefore(expiresAt);
    }
}
