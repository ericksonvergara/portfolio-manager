package com.portafolio_manager.backend.infrastructure.persistence.auth;

import com.portafolio_manager.backend.application.port.out.auth.RefreshTokenRepositoryPort;
import com.portafolio_manager.backend.domain.auth.RefreshToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Component
class RefreshTokenPersistenceAdapter implements RefreshTokenRepositoryPort {

    private final RefreshTokenJpaRepository repository;

    RefreshTokenPersistenceAdapter(RefreshTokenJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenJpaEntity entity = new RefreshTokenJpaEntity(
                refreshToken.id(),
                refreshToken.userId(),
                refreshToken.tokenHash(),
                refreshToken.expiresAt(),
                refreshToken.revokedAt()
        );

        return toDomain(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return repository.findByTokenHash(tokenHash).map(RefreshTokenPersistenceAdapter::toDomain);
    }

    @Override
    @Transactional
    public boolean revoke(Long refreshTokenId, Instant revokedAt) {
        return repository.revokeIfActive(refreshTokenId, revokedAt) == 1;
    }

    @Override
    @Transactional
    public void revokeAllByUserId(Long userId, Instant revokedAt) {
        repository.revokeAllActiveByUserId(userId, revokedAt);
    }

    private static RefreshToken toDomain(RefreshTokenJpaEntity entity) {
        return new RefreshToken(
                entity.getId(),
                entity.getUserId(),
                entity.getTokenHash(),
                entity.getExpiresAt(),
                entity.getRevokedAt()
        );
    }
}
