package com.portafolio_manager.backend.infrastructure.persistence.auth;

import com.portafolio_manager.backend.application.port.out.auth.RevokedAccessTokenRepositoryPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
class RevokedAccessTokenPersistenceAdapter implements RevokedAccessTokenRepositoryPort {

    private final RevokedAccessTokenJpaRepository repository;

    RevokedAccessTokenPersistenceAdapter(RevokedAccessTokenJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void revoke(String tokenId, Instant expiresAt) {
        repository.insertIfAbsent(tokenId, expiresAt);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isRevoked(String tokenId) {
        return repository.existsById(tokenId);
    }

    @Override
    @Transactional
    public void deleteExpired(Instant now) {
        repository.deleteExpired(now);
    }
}
