package com.portafolio_manager.backend.application.service.auth;

import com.portafolio_manager.backend.application.port.in.auth.AuthenticationResult;
import com.portafolio_manager.backend.application.port.out.auth.AccessToken;
import com.portafolio_manager.backend.application.port.out.auth.AccessTokenProviderPort;
import com.portafolio_manager.backend.application.port.out.auth.RefreshTokenGeneratorPort;
import com.portafolio_manager.backend.application.port.out.auth.RefreshTokenRepositoryPort;
import com.portafolio_manager.backend.domain.auth.RefreshToken;
import com.portafolio_manager.backend.domain.user.User;

import java.time.Duration;
import java.time.Instant;

public class SessionIssuer {

    private final AccessTokenProviderPort accessTokenProvider;
    private final RefreshTokenGeneratorPort refreshTokenGenerator;
    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final Duration refreshTokenTimeToLive;

    public SessionIssuer(
            AccessTokenProviderPort accessTokenProvider,
            RefreshTokenGeneratorPort refreshTokenGenerator,
            RefreshTokenRepositoryPort refreshTokenRepository,
            Duration refreshTokenTimeToLive
    ) {
        this.accessTokenProvider = accessTokenProvider;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenTimeToLive = refreshTokenTimeToLive;
    }

    AuthenticationResult issue(User user, Instant now) {
        AccessToken accessToken = accessTokenProvider.generate(user);

        String rawRefreshToken = refreshTokenGenerator.generate();
        RefreshToken refreshToken = refreshTokenRepository.save(RefreshToken.issue(
                user.id(),
                refreshTokenGenerator.hash(rawRefreshToken),
                now,
                refreshTokenTimeToLive
        ));

        return new AuthenticationResult(
                accessToken.value(),
                accessToken.expiresAt(),
                rawRefreshToken,
                refreshToken.expiresAt()
        );
    }
}
