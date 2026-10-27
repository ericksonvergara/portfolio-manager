package com.portafolio_manager.backend.application.service.auth;

import com.portafolio_manager.backend.application.port.in.auth.ResetPasswordCommand;
import com.portafolio_manager.backend.application.port.out.auth.PasswordHasherPort;
import com.portafolio_manager.backend.application.port.out.auth.PasswordResetTokenRepositoryPort;
import com.portafolio_manager.backend.application.port.out.auth.RefreshTokenGeneratorPort;
import com.portafolio_manager.backend.application.port.out.auth.RefreshTokenRepositoryPort;
import com.portafolio_manager.backend.application.port.out.user.UserRepositoryPort;
import com.portafolio_manager.backend.domain.auth.InvalidPasswordResetTokenException;
import com.portafolio_manager.backend.domain.auth.PasswordResetToken;
import com.portafolio_manager.backend.domain.user.WeakPasswordException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ResetPasswordServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-22T12:00:00Z");

    private final PasswordResetTokenRepositoryPort tokenRepository = mock(PasswordResetTokenRepositoryPort.class);
    private final RefreshTokenGeneratorPort tokenGenerator = mock(RefreshTokenGeneratorPort.class);
    private final UserRepositoryPort userRepository = mock(UserRepositoryPort.class);
    private final PasswordHasherPort passwordHasher = mock(PasswordHasherPort.class);
    private final RefreshTokenRepositoryPort refreshTokenRepository = mock(RefreshTokenRepositoryPort.class);
    private final ResetPasswordService service = new ResetPasswordService(
            tokenRepository, tokenGenerator, userRepository, passwordHasher, refreshTokenRepository,
            Clock.fixed(NOW, ZoneOffset.UTC)
    );

    @Test
    void shouldSetTheNewPasswordAndCloseEverySession() {
        givenToken(new PasswordResetToken(5L, 1L, "hashed", NOW.plusSeconds(60), null));
        when(tokenRepository.markUsed(5L, NOW)).thenReturn(true);
        when(passwordHasher.hash("Nueva#123")).thenReturn("bcrypt");

        service.execute(new ResetPasswordCommand("raw", "Nueva#123"));

        verify(userRepository).updatePassword(1L, "bcrypt");
        verify(refreshTokenRepository).revokeAllByUserId(1L, NOW);
    }

    @Test
    void shouldRejectAPasswordOutsideThePolicyWithoutUsingTheLink() {
        // RF004
        assertThatThrownBy(() -> service.execute(new ResetPasswordCommand("raw", "corta")))
                .isInstanceOf(WeakPasswordException.class);
        verifyNoInteractions(tokenRepository, userRepository);
    }

    @Test
    void shouldRejectUnknownExpiredOrUsedLinks() {
        when(tokenGenerator.hash(anyString())).thenReturn("hashed");
        when(tokenRepository.findByTokenHash("hashed")).thenReturn(
                Optional.empty(),
                Optional.of(new PasswordResetToken(5L, 1L, "hashed", NOW, null)),              // expires now
                Optional.of(new PasswordResetToken(5L, 1L, "hashed", NOW.plusSeconds(60), NOW)) // already used
        );

        for (int attempt = 0; attempt < 3; attempt++) {
            assertThatThrownBy(() -> service.execute(new ResetPasswordCommand("raw", "Nueva#123")))
                    .isInstanceOf(InvalidPasswordResetTokenException.class);
        }
        assertThatThrownBy(() -> service.execute(new ResetPasswordCommand(" ", "Nueva#123")))
                .isInstanceOf(InvalidPasswordResetTokenException.class);
        verify(userRepository, never()).updatePassword(anyLong(), anyString());
    }

    @Test
    void shouldLetOnlyOneOfTwoSimultaneousRequestsUseTheLink() {
        givenToken(new PasswordResetToken(5L, 1L, "hashed", NOW.plusSeconds(60), null));
        when(tokenRepository.markUsed(5L, NOW)).thenReturn(false);

        assertThatThrownBy(() -> service.execute(new ResetPasswordCommand("raw", "Nueva#123")))
                .isInstanceOf(InvalidPasswordResetTokenException.class);
        verify(userRepository, never()).updatePassword(anyLong(), any());
    }

    private void givenToken(PasswordResetToken token) {
        when(tokenGenerator.hash("raw")).thenReturn("hashed");
        when(tokenRepository.findByTokenHash("hashed")).thenReturn(Optional.of(token));
    }
}
