package com.portafolio_manager.backend.application.service.auth;

import com.portafolio_manager.backend.application.port.in.auth.LogoutCommand;
import com.portafolio_manager.backend.application.port.in.auth.LogoutUseCase;
import com.portafolio_manager.backend.application.port.out.auth.AccessTokenProviderPort;
import com.portafolio_manager.backend.application.port.out.auth.RefreshTokenGeneratorPort;
import com.portafolio_manager.backend.application.port.out.auth.RefreshTokenRepositoryPort;
import com.portafolio_manager.backend.application.port.out.auth.RevokedAccessTokenRepositoryPort;
import com.portafolio_manager.backend.domain.auth.RefreshToken;

import java.time.Clock;
import java.time.Instant;

public class LogoutService implements LogoutUseCase {

    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final RefreshTokenGeneratorPort refreshTokenGenerator;
    private final AccessTokenProviderPort accessTokenProvider;
    private final RevokedAccessTokenRepositoryPort revokedAccessTokenRepository;
    private final Clock clock;

    public LogoutService(
            RefreshTokenRepositoryPort refreshTokenRepository,
            RefreshTokenGeneratorPort refreshTokenGenerator,
            AccessTokenProviderPort accessTokenProvider,
            RevokedAccessTokenRepositoryPort revokedAccessTokenRepository,
            Clock clock
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.accessTokenProvider = accessTokenProvider;
        this.revokedAccessTokenRepository = revokedAccessTokenRepository;
        this.clock = clock;
    }

    @Override
    public void execute(LogoutCommand command) {
        Instant now = clock.instant();
        revokeRefreshToken(command.refreshToken(), now);
        revokeAccessToken(command.accessToken(), now);
    }

    private void revokeRefreshToken(String rawRefreshToken, Instant now) {
        if (isBlank(rawRefreshToken)) {
            return;
        }

        refreshTokenRepository.findByTokenHash(refreshTokenGenerator.hash(rawRefreshToken))
                .filter(refreshToken -> !refreshToken.isRevoked())
                .map(RefreshToken::id)
                .ifPresent(refreshTokenId -> refreshTokenRepository.revoke(refreshTokenId, now));
    }

    private void revokeAccessToken(String rawAccessToken, Instant now) {
        if (isBlank(rawAccessToken)) {
            return;
        }

        // An expired or invalid access token is already unusable, so there is nothing to revoke.
        accessTokenProvider.parse(rawAccessToken)
                .ifPresent(accessToken -> revokedAccessTokenRepository.revoke(accessToken.id(), accessToken.expiresAt()));
        revokedAccessTokenRepository.deleteExpired(now);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
