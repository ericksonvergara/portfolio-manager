package com.portafolio_manager.backend.application.service.auth;

import com.portafolio_manager.backend.application.port.in.auth.RequestPasswordResetUseCase;
import com.portafolio_manager.backend.application.port.out.auth.PasswordResetMailerPort;
import com.portafolio_manager.backend.application.port.out.auth.PasswordResetTokenRepositoryPort;
import com.portafolio_manager.backend.application.port.out.auth.RefreshTokenGeneratorPort;
import com.portafolio_manager.backend.application.port.out.user.UserRepositoryPort;
import com.portafolio_manager.backend.domain.auth.PasswordResetToken;
import com.portafolio_manager.backend.domain.user.User;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Issues a recovery link. The token is an opaque random value, like a refresh token, so it reuses
 * the same generator; only its hash is stored.
 */
public class RequestPasswordResetService implements RequestPasswordResetUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordResetTokenRepositoryPort tokenRepository;
    private final RefreshTokenGeneratorPort tokenGenerator;
    private final PasswordResetMailerPort mailer;
    private final Clock clock;
    private final Duration validity;
    private final String resetUrl;

    public RequestPasswordResetService(
            UserRepositoryPort userRepository,
            PasswordResetTokenRepositoryPort tokenRepository,
            RefreshTokenGeneratorPort tokenGenerator,
            PasswordResetMailerPort mailer,
            Clock clock,
            Duration validity,
            String resetUrl
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.tokenGenerator = tokenGenerator;
        this.mailer = mailer;
        this.clock = clock;
        this.validity = validity;
        this.resetUrl = resetUrl;
    }

    @Override
    public void execute(String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        userRepository.findByEmail(User.normalizeEmail(email))
                .filter(User::active)
                .ifPresent(this::sendLink);
    }

    private void sendLink(User user) {
        Instant now = clock.instant();
        tokenRepository.invalidateAllByUserId(user.id(), now);

        String rawToken = tokenGenerator.generate();
        tokenRepository.save(PasswordResetToken.issue(user.id(), tokenGenerator.hash(rawToken), now.plus(validity)));

        String link = resetUrl + "?token=" + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
        mailer.sendPasswordReset(user.email(), user.name(), link, validity);
    }
}
