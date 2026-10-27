package com.portafolio_manager.backend.application.service.auth;

import com.portafolio_manager.backend.application.port.out.auth.PasswordResetMailerPort;
import com.portafolio_manager.backend.application.port.out.auth.PasswordResetTokenRepositoryPort;
import com.portafolio_manager.backend.application.port.out.auth.RefreshTokenGeneratorPort;
import com.portafolio_manager.backend.application.port.out.user.UserRepositoryPort;
import com.portafolio_manager.backend.domain.auth.PasswordResetToken;
import com.portafolio_manager.backend.domain.user.Role;
import com.portafolio_manager.backend.domain.user.User;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RequestPasswordResetServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-22T12:00:00Z");

    private final UserRepositoryPort userRepository = mock(UserRepositoryPort.class);
    private final PasswordResetTokenRepositoryPort tokenRepository = mock(PasswordResetTokenRepositoryPort.class);
    private final RefreshTokenGeneratorPort tokenGenerator = mock(RefreshTokenGeneratorPort.class);
    private final PasswordResetMailerPort mailer = mock(PasswordResetMailerPort.class);
    private final RequestPasswordResetService service = new RequestPasswordResetService(
            userRepository, tokenRepository, tokenGenerator, mailer,
            Clock.fixed(NOW, ZoneOffset.UTC), Duration.ofMinutes(30), "http://localhost:8081/restablecer-contrasena"
    );

    @Test
    void shouldSendASingleUseLinkThatExpires() {
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user(true)));
        when(tokenGenerator.generate()).thenReturn("raw+token/1");
        when(tokenGenerator.hash("raw+token/1")).thenReturn("hashed");

        service.execute("  Admin@Example.com ");

        ArgumentCaptor<PasswordResetToken> saved = ArgumentCaptor.forClass(PasswordResetToken.class);
        var order = inOrder(tokenRepository, mailer);
        // The previous links stop working before the new one is issued.
        order.verify(tokenRepository).invalidateAllByUserId(1L, NOW);
        order.verify(tokenRepository).save(saved.capture());
        order.verify(mailer).sendPasswordReset("admin@example.com", "Admin",
                "http://localhost:8081/restablecer-contrasena?token=raw%2Btoken%2F1", Duration.ofMinutes(30));
        assertThat(saved.getValue().tokenHash()).isEqualTo("hashed"); // never the raw token
        assertThat(saved.getValue().expiresAt()).isEqualTo(NOW.plus(Duration.ofMinutes(30)));
    }

    @Test
    void shouldDoNothingForAnUnknownOrInactiveAccount() {
        when(userRepository.findByEmail("nadie@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("inactivo@example.com")).thenReturn(Optional.of(user(false)));

        service.execute("nadie@example.com");
        service.execute("inactivo@example.com");
        service.execute(" ");

        verifyNoInteractions(tokenRepository, mailer);
    }

    private static User user(boolean active) {
        return new User(1L, "Admin", "admin@example.com", "hash", Set.of(Role.ADMIN), active, null);
    }
}
