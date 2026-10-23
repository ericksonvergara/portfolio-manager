package com.portafolio_manager.backend.infrastructure.persistence.auth;

import com.portafolio_manager.backend.application.port.out.auth.PasswordResetTokenRepositoryPort;
import com.portafolio_manager.backend.domain.auth.PasswordResetToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Component
class PasswordResetTokenPersistenceAdapter implements PasswordResetTokenRepositoryPort {

    private final PasswordResetTokenJpaRepository repository;

    PasswordResetTokenPersistenceAdapter(PasswordResetTokenJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public PasswordResetToken save(PasswordResetToken token) {
        return toDomain(repository.save(new PasswordResetTokenJpaEntity(
                token.id(), token.userId(), token.tokenHash(), token.expiresAt(), token.usedAt())));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PasswordResetToken> findByTokenHash(String tokenHash) {
        return repository.findByTokenHash(tokenHash).map(PasswordResetTokenPersistenceAdapter::toDomain);
    }

    @Override
    @Transactional
    public void invalidateAllByUserId(Long userId, Instant now) {
        repository.markAllUnusedAsUsed(userId, now);
    }

    @Override
    @Transactional
    public boolean markUsed(Long tokenId, Instant usedAt) {
        return repository.markUsedIfUnused(tokenId, usedAt) == 1;
    }

    private static PasswordResetToken toDomain(PasswordResetTokenJpaEntity entity) {
        return new PasswordResetToken(
                entity.getId(), entity.getUserId(), entity.getTokenHash(), entity.getExpiresAt(), entity.getUsedAt());
    }
}
