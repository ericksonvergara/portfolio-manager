package com.portafolio_manager.backend.application.service.auth;

import com.portafolio_manager.backend.application.port.in.auth.AuthenticationResult;
import com.portafolio_manager.backend.application.port.in.auth.RefreshSessionUseCase;
import com.portafolio_manager.backend.application.port.out.auth.RefreshTokenGeneratorPort;
import com.portafolio_manager.backend.application.port.out.auth.RefreshTokenRepositoryPort;
import com.portafolio_manager.backend.application.port.out.user.UserRepositoryPort;
import com.portafolio_manager.backend.domain.auth.InvalidRefreshTokenException;
import com.portafolio_manager.backend.domain.auth.RefreshToken;
import com.portafolio_manager.backend.domain.user.User;

import java.time.Clock;
import java.time.Instant;

public class RefreshSessionService implements RefreshSessionUseCase {

    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final RefreshTokenGeneratorPort refreshTokenGenerator;
    private final UserRepositoryPort userRepository;
    private final SessionIssuer sessionIssuer;
    private final Clock clock;

    public RefreshSessionService(
            RefreshTokenRepositoryPort refreshTokenRepository,
            RefreshTokenGeneratorPort refreshTokenGenerator,
            UserRepositoryPort userRepository,
            SessionIssuer sessionIssuer,
            Clock clock
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.userRepository = userRepository;
        this.sessionIssuer = sessionIssuer;
        this.clock = clock;
    }

    @Override
    public AuthenticationResult execute(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new InvalidRefreshTokenException();
        }

        Instant now = clock.instant();
        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenHash(refreshTokenGenerator.hash(rawRefreshToken))
                .orElseThrow(InvalidRefreshTokenException::new);

        if (refreshToken.isRevoked()) {
            // Reuse of a rotated token means it may have been stolen: close every session of the user.
            refreshTokenRepository.revokeAllByUserId(refreshToken.userId(), now);
            throw new InvalidRefreshTokenException();
        }

        if (refreshToken.isExpired(now) || !refreshTokenRepository.revoke(refreshToken.id(), now)) {
            throw new InvalidRefreshTokenException();
        }

        User user = userRepository.findById(refreshToken.userId())
                .filter(User::active)
                .orElseThrow(InvalidRefreshTokenException::new);

        return sessionIssuer.issue(user, now);
    }
}
