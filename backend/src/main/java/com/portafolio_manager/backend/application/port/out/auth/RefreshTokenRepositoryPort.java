package com.portafolio_manager.backend.application.port.out.auth;

import com.portafolio_manager.backend.application.port.out.PersistencePort;
import com.portafolio_manager.backend.domain.auth.RefreshToken;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepositoryPort extends PersistencePort {

    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Revokes the token only if it is still active.
     *
     * @return {@code true} if this call revoked the token, {@code false} if it was already revoked
     */
    boolean revoke(Long refreshTokenId, Instant revokedAt);

    void revokeAllByUserId(Long userId, Instant revokedAt);
}
