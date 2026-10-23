package com.portafolio_manager.backend.application.service.auth;

import com.portafolio_manager.backend.application.port.in.auth.ChangePasswordCommand;
import com.portafolio_manager.backend.application.port.out.auth.PasswordHasherPort;
import com.portafolio_manager.backend.application.port.out.auth.RefreshTokenRepositoryPort;
import com.portafolio_manager.backend.application.port.out.auth.RevokedAccessTokenRepositoryPort;
import com.portafolio_manager.backend.application.port.out.user.UserRepositoryPort;
import com.portafolio_manager.backend.domain.user.IncorrectPasswordException;
import com.portafolio_manager.backend.domain.user.Role;
import com.portafolio_manager.backend.domain.user.UnchangedPasswordException;
import com.portafolio_manager.backend.domain.user.User;
import com.portafolio_manager.backend.domain.user.WeakPasswordException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ChangePasswordServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-23T12:00:00Z");
    private static final Instant TOKEN_EXPIRY = NOW.plusSeconds(600);

    private final UserRepositoryPort userRepository = mock(UserRepositoryPort.class);
    private final PasswordHasherPort passwordHasher = mock(PasswordHasherPort.class);
    private final RefreshTokenRepositoryPort refreshTokenRepository = mock(RefreshTokenRepositoryPort.class);
    private final RevokedAccessTokenRepositoryPort revokedAccessTokenRepository = mock(RevokedAccessTokenRepositoryPort.class);
    private final ChangePasswordService service = new ChangePasswordService(
            userRepository, passwordHasher, refreshTokenRepository, revokedAccessTokenRepository,
            Clock.fixed(NOW, ZoneOffset.UTC)
    );

    @BeforeEach
    void givenTheSignedInUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(
                new User(1L, "Admin", "admin@example.com", "bcrypt-actual", Set.of(Role.ADMIN), true, null)));
        when(passwordHasher.matches("Actual#123", "bcrypt-actual")).thenReturn(true);
    }

    @Test
    void shouldStoreTheNewPasswordWithBcryptAndCloseEverySession() {
        when(passwordHasher.hash("Nueva#456")).thenReturn("bcrypt-nueva");

        service.execute(command("Actual#123", "Nueva#456"));

        verify(userRepository).updatePassword(1L, "bcrypt-nueva");
        verify(refreshTokenRepository).revokeAllByUserId(1L, NOW);
        verify(revokedAccessTokenRepository).revoke("jti-1", TOKEN_EXPIRY);
    }

    @Test
    void shouldRequireTheCurrentPassword() {
        assertThatThrownBy(() -> service.execute(command("Equivocada#1", "Nueva#456")))
                .isInstanceOf(IncorrectPasswordException.class);
        assertThatThrownBy(() -> service.execute(command(null, "Nueva#456")))
                .isInstanceOf(IncorrectPasswordException.class);
        verifyNothingChanged();
    }

    @Test
    void shouldApplyThePasswordPolicyAndRejectTheSamePassword() {
        // RF004
        assertThatThrownBy(() -> service.execute(command("Actual#123", "corta")))
                .isInstanceOf(WeakPasswordException.class);
        when(passwordHasher.matches("Actual#123", "bcrypt-actual")).thenReturn(true);
        assertThatThrownBy(() -> service.execute(command("Actual#123", "Actual#123")))
                .isInstanceOf(UnchangedPasswordException.class);
        verifyNothingChanged();
    }

    @Test
    void shouldRejectAnUnknownOrInactiveUser() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new ChangePasswordCommand(2L, "Actual#123", "Nueva#456", "jti", TOKEN_EXPIRY)))
                .isInstanceOf(IncorrectPasswordException.class);
        verifyNothingChanged();
    }

    private void verifyNothingChanged() {
        verify(userRepository, never()).updatePassword(anyLong(), anyString());
        verifyNoInteractions(refreshTokenRepository, revokedAccessTokenRepository);
    }

    private static ChangePasswordCommand command(String current, String next) {
        return new ChangePasswordCommand(1L, current, next, "jti-1", TOKEN_EXPIRY);
    }
}
