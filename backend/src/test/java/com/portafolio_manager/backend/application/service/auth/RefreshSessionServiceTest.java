package com.portafolio_manager.backend.application.service.auth;

import com.portafolio_manager.backend.application.port.in.auth.AuthenticationResult;
import com.portafolio_manager.backend.application.port.out.auth.RefreshTokenGeneratorPort;
import com.portafolio_manager.backend.application.port.out.auth.RefreshTokenRepositoryPort;
import com.portafolio_manager.backend.application.port.out.user.UserRepositoryPort;
import com.portafolio_manager.backend.domain.auth.InvalidRefreshTokenException;
import com.portafolio_manager.backend.domain.auth.RefreshToken;
import com.portafolio_manager.backend.domain.user.Role;
import com.portafolio_manager.backend.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshSessionServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-09T12:00:00Z");
    private static final User ADMIN = new User(
            1L, "Admin", "admin@example.com", "hash", Set.of(Role.ADMIN), true, null
    );

    @Mock
    private RefreshTokenRepositoryPort refreshTokenRepository;

    @Mock
    private RefreshTokenGeneratorPort refreshTokenGenerator;

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private SessionIssuer sessionIssuer;

    private RefreshSessionService refreshSessionService;

    @BeforeEach
    void setUp() {
        refreshSessionService = new RefreshSessionService(
                refreshTokenRepository,
                refreshTokenGenerator,
                userRepository,
                sessionIssuer,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldRotateValidRefreshToken() {
        RefreshToken stored = new RefreshToken(10L, 1L, "hashed", NOW.plusSeconds(60), null);
        AuthenticationResult expected = new AuthenticationResult("access", NOW, "new-refresh", NOW);
        when(refreshTokenGenerator.hash("raw")).thenReturn("hashed");
        when(refreshTokenRepository.findByTokenHash("hashed")).thenReturn(Optional.of(stored));
        when(refreshTokenRepository.revoke(10L, NOW)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(ADMIN));
        when(sessionIssuer.issue(ADMIN, NOW)).thenReturn(expected);

        assertThat(refreshSessionService.execute("raw")).isEqualTo(expected);
    }

    @Test
    void shouldRevokeAllSessionsWhenRevokedTokenIsReused() {
        RefreshToken revoked = new RefreshToken(10L, 1L, "hashed", NOW.plusSeconds(60), NOW.minusSeconds(5));
        when(refreshTokenGenerator.hash("raw")).thenReturn("hashed");
        when(refreshTokenRepository.findByTokenHash("hashed")).thenReturn(Optional.of(revoked));

        assertThatThrownBy(() -> refreshSessionService.execute("raw"))
                .isInstanceOf(InvalidRefreshTokenException.class);
        verify(refreshTokenRepository).revokeAllByUserId(1L, NOW);
        verify(sessionIssuer, never()).issue(any(), any());
    }

    @Test
    void shouldRejectExpiredToken() {
        RefreshToken expired = new RefreshToken(10L, 1L, "hashed", NOW, null);
        when(refreshTokenGenerator.hash("raw")).thenReturn("hashed");
        when(refreshTokenRepository.findByTokenHash("hashed")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> refreshSessionService.execute("raw"))
                .isInstanceOf(InvalidRefreshTokenException.class);
        verify(refreshTokenRepository, never()).revoke(any(), any());
    }

    @Test
    void shouldRejectTokenRevokedConcurrently() {
        RefreshToken stored = new RefreshToken(10L, 1L, "hashed", NOW.plusSeconds(60), null);
        when(refreshTokenGenerator.hash("raw")).thenReturn("hashed");
        when(refreshTokenRepository.findByTokenHash("hashed")).thenReturn(Optional.of(stored));
        when(refreshTokenRepository.revoke(10L, NOW)).thenReturn(false);

        assertThatThrownBy(() -> refreshSessionService.execute("raw"))
                .isInstanceOf(InvalidRefreshTokenException.class);
        verify(sessionIssuer, never()).issue(any(), any());
    }

    @Test
    void shouldRejectUnknownOrMissingToken() {
        when(refreshTokenGenerator.hash("unknown")).thenReturn("unknown-hash");
        when(refreshTokenRepository.findByTokenHash("unknown-hash")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshSessionService.execute("unknown"))
                .isInstanceOf(InvalidRefreshTokenException.class);
        assertThatThrownBy(() -> refreshSessionService.execute(null))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }
}
