package com.portafolio_manager.backend.application.service.auth;

import com.portafolio_manager.backend.application.port.in.auth.LogoutCommand;
import com.portafolio_manager.backend.application.port.out.auth.AccessToken;
import com.portafolio_manager.backend.application.port.out.auth.AccessTokenProviderPort;
import com.portafolio_manager.backend.application.port.out.auth.RefreshTokenGeneratorPort;
import com.portafolio_manager.backend.application.port.out.auth.RefreshTokenRepositoryPort;
import com.portafolio_manager.backend.application.port.out.auth.RevokedAccessTokenRepositoryPort;
import com.portafolio_manager.backend.domain.auth.RefreshToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-09T12:00:00Z");

    @Mock
    private RefreshTokenRepositoryPort refreshTokenRepository;

    @Mock
    private RefreshTokenGeneratorPort refreshTokenGenerator;

    @Mock
    private AccessTokenProviderPort accessTokenProvider;

    @Mock
    private RevokedAccessTokenRepositoryPort revokedAccessTokenRepository;

    private LogoutService logoutService;

    @BeforeEach
    void setUp() {
        logoutService = new LogoutService(
                refreshTokenRepository,
                refreshTokenGenerator,
                accessTokenProvider,
                revokedAccessTokenRepository,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldRevokeBothRefreshAndAccessTokens() {
        Instant accessExpiry = NOW.plusSeconds(900);
        when(refreshTokenGenerator.hash("raw-refresh")).thenReturn("hashed");
        when(refreshTokenRepository.findByTokenHash("hashed"))
                .thenReturn(Optional.of(new RefreshToken(10L, 1L, "hashed", NOW.plusSeconds(60), null)));
        when(accessTokenProvider.parse("raw-access"))
                .thenReturn(Optional.of(new AccessToken("raw-access", "jti-1", accessExpiry)));

        logoutService.execute(new LogoutCommand("raw-refresh", "raw-access"));

        verify(refreshTokenRepository).revoke(10L, NOW);
        verify(revokedAccessTokenRepository).revoke("jti-1", accessExpiry);
        verify(revokedAccessTokenRepository).deleteExpired(NOW);
    }

    @Test
    void shouldSkipAlreadyRevokedRefreshToken() {
        when(refreshTokenGenerator.hash("raw-refresh")).thenReturn("hashed");
        when(refreshTokenRepository.findByTokenHash("hashed"))
                .thenReturn(Optional.of(new RefreshToken(10L, 1L, "hashed", NOW.plusSeconds(60), NOW)));

        logoutService.execute(new LogoutCommand("raw-refresh", null));

        verify(refreshTokenRepository, never()).revoke(any(), any());
    }

    @Test
    void shouldNotStoreExpiredOrInvalidAccessToken() {
        when(accessTokenProvider.parse("expired-access")).thenReturn(Optional.empty());

        logoutService.execute(new LogoutCommand(null, "expired-access"));

        verify(revokedAccessTokenRepository, never()).revoke(any(), any());
    }

    @Test
    void shouldDoNothingWithoutTokens() {
        logoutService.execute(new LogoutCommand(null, " "));

        verifyNoInteractions(refreshTokenRepository, refreshTokenGenerator, accessTokenProvider, revokedAccessTokenRepository);
    }
}
