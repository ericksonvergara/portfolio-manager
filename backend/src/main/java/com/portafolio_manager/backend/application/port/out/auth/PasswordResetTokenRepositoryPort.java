package com.portafolio_manager.backend.application.port.out.auth;

import com.portafolio_manager.backend.application.port.out.PersistencePort;
import com.portafolio_manager.backend.domain.auth.PasswordResetToken;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepositoryPort extends PersistencePort {

    PasswordResetToken save(PasswordResetToken token);

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    /** Marks every unused token of the user as used, so only the newest link works. */
    void invalidateAllByUserId(Long userId, Instant now);

    /**
     * Marks the token as used only if it still was unused.
     *
     * @return {@code true} if this call used it, {@code false} if it had already been used
     */
    boolean markUsed(Long tokenId, Instant usedAt);
}
