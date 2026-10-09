package com.portafolio_manager.backend.application.port.out.auth;

import com.portafolio_manager.backend.application.port.out.PersistencePort;

import java.time.Instant;

public interface RevokedAccessTokenRepositoryPort extends PersistencePort {

    void revoke(String tokenId, Instant expiresAt);

    boolean isRevoked(String tokenId);

    void deleteExpired(Instant now);
}
