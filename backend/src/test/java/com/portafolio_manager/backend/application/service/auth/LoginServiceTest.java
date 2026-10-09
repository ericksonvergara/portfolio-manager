package com.portafolio_manager.backend.application.service.auth;

import com.portafolio_manager.backend.application.port.in.auth.AuthenticationResult;
import com.portafolio_manager.backend.application.port.in.auth.LoginCommand;
import com.portafolio_manager.backend.application.port.out.auth.PasswordHasherPort;
import com.portafolio_manager.backend.application.port.out.user.UserRepositoryPort;
import com.portafolio_manager.backend.domain.auth.InvalidCredentialsException;
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
class LoginServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-09T12:00:00Z");
    private static final User ADMIN = new User(
            1L, "Admin", "admin@example.com", "hash", Set.of(Role.ADMIN), true, null
    );

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private PasswordHasherPort passwordHasher;

    @Mock
    private SessionIssuer sessionIssuer;

    private LoginService loginService;

    @BeforeEach
    void setUp() {
        loginService = new LoginService(
                userRepository,
                passwordHasher,
                sessionIssuer,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldIssueSessionAndRecordLastLoginWhenCredentialsAreValid() {
        AuthenticationResult expected = new AuthenticationResult("access", NOW, "refresh", NOW);
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(ADMIN));
        when(passwordHasher.matches("Secret#1", "hash")).thenReturn(true);
        when(sessionIssuer.issue(ADMIN, NOW)).thenReturn(expected);

        AuthenticationResult result = loginService.execute(new LoginCommand(" Admin@Example.com ", "Secret#1"));

        assertThat(result).isEqualTo(expected);
        verify(userRepository).updateLastLogin(1L, NOW);
    }

    @Test
    void shouldRejectWrongPassword() {
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(ADMIN));
        when(passwordHasher.matches("Wrong#1", "hash")).thenReturn(false);

        assertThatThrownBy(() -> loginService.execute(new LoginCommand("admin@example.com", "Wrong#1")))
                .isInstanceOf(InvalidCredentialsException.class);
        verify(sessionIssuer, never()).issue(any(), any());
    }

    @Test
    void shouldRejectUnknownEmail() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginService.execute(new LoginCommand("nobody@example.com", "Secret#1")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void shouldRejectInactiveUser() {
        User inactive = new User(1L, "Admin", "admin@example.com", "hash", Set.of(Role.ADMIN), false, null);
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> loginService.execute(new LoginCommand("admin@example.com", "Secret#1")))
                .isInstanceOf(InvalidCredentialsException.class);
        verify(userRepository, never()).updateLastLogin(any(), any());
    }
}
