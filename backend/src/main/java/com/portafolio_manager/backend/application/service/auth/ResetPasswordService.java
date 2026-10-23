package com.portafolio_manager.backend.application.service.auth;

import com.portafolio_manager.backend.application.port.in.auth.ResetPasswordCommand;
import com.portafolio_manager.backend.application.port.in.auth.ResetPasswordUseCase;
import com.portafolio_manager.backend.application.port.out.auth.PasswordHasherPort;
import com.portafolio_manager.backend.application.port.out.auth.PasswordResetTokenRepositoryPort;
import com.portafolio_manager.backend.application.port.out.auth.RefreshTokenGeneratorPort;
import com.portafolio_manager.backend.application.port.out.auth.RefreshTokenRepositoryPort;
import com.portafolio_manager.backend.application.port.out.user.UserRepositoryPort;
import com.portafolio_manager.backend.domain.auth.InvalidPasswordResetTokenException;
import com.portafolio_manager.backend.domain.auth.PasswordResetToken;
import com.portafolio_manager.backend.domain.user.PasswordPolicy;
import com.portafolio_manager.backend.domain.user.WeakPasswordException;

import java.time.Clock;
import java.time.Instant;

public class ResetPasswordService implements ResetPasswordUseCase {

    private final PasswordResetTokenRepositoryPort tokenRepository;
    private final RefreshTokenGeneratorPort tokenGenerator;
    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;
    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final Clock clock;

    public ResetPasswordService(
            PasswordResetTokenRepositoryPort tokenRepository,
            RefreshTokenGeneratorPort tokenGenerator,
            UserRepositoryPort userRepository,
            PasswordHasherPort passwordHasher,
            RefreshTokenRepositoryPort refreshTokenRepository,
            Clock clock
    ) {
        this.tokenRepository = tokenRepository;
        this.tokenGenerator = tokenGenerator;
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.refreshTokenRepository = refreshTokenRepository;
        this.clock = clock;
    }

    @Override
    public void execute(ResetPasswordCommand command) {
        if (!PasswordPolicy.isSatisfiedBy(command.newPassword())) {
            throw new WeakPasswordException();
        }
        if (command.token() == null || command.token().isBlank()) {
            throw new InvalidPasswordResetTokenException();
        }

        Instant now = clock.instant();
        PasswordResetToken token = tokenRepository.findByTokenHash(tokenGenerator.hash(command.token()))
                .filter(found -> found.isUsableAt(now))
                .orElseThrow(InvalidPasswordResetTokenException::new);
        // Two simultaneous requests with the same link: only the first one wins.
        if (!tokenRepository.markUsed(token.id(), now)) {
            throw new InvalidPasswordResetTokenException();
        }

        userRepository.updatePassword(token.userId(), passwordHasher.hash(command.newPassword()));
        // Whoever knew the old password loses every session it had opened.
        refreshTokenRepository.revokeAllByUserId(token.userId(), now);
    }
}
