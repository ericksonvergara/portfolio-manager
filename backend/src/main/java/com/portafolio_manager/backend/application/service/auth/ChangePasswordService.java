package com.portafolio_manager.backend.application.service.auth;

import com.portafolio_manager.backend.application.port.in.auth.ChangePasswordCommand;
import com.portafolio_manager.backend.application.port.in.auth.ChangePasswordUseCase;
import com.portafolio_manager.backend.application.port.out.auth.PasswordHasherPort;
import com.portafolio_manager.backend.application.port.out.auth.RefreshTokenRepositoryPort;
import com.portafolio_manager.backend.application.port.out.auth.RevokedAccessTokenRepositoryPort;
import com.portafolio_manager.backend.application.port.out.user.UserRepositoryPort;
import com.portafolio_manager.backend.domain.user.IncorrectPasswordException;
import com.portafolio_manager.backend.domain.user.PasswordPolicy;
import com.portafolio_manager.backend.domain.user.UnchangedPasswordException;
import com.portafolio_manager.backend.domain.user.User;
import com.portafolio_manager.backend.domain.user.WeakPasswordException;

import java.time.Clock;
import java.time.Instant;

public class ChangePasswordService implements ChangePasswordUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;
    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final RevokedAccessTokenRepositoryPort revokedAccessTokenRepository;
    private final Clock clock;

    public ChangePasswordService(
            UserRepositoryPort userRepository,
            PasswordHasherPort passwordHasher,
            RefreshTokenRepositoryPort refreshTokenRepository,
            RevokedAccessTokenRepositoryPort revokedAccessTokenRepository,
            Clock clock
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.refreshTokenRepository = refreshTokenRepository;
        this.revokedAccessTokenRepository = revokedAccessTokenRepository;
        this.clock = clock;
    }

    @Override
    public void execute(ChangePasswordCommand command) {
        User user = userRepository.findById(command.userId())
                .filter(User::active)
                .orElseThrow(IncorrectPasswordException::new);
        if (command.currentPassword() == null
                || !passwordHasher.matches(command.currentPassword(), user.passwordHash())) {
            throw new IncorrectPasswordException();
        }
        if (!PasswordPolicy.isSatisfiedBy(command.newPassword())) {
            throw new WeakPasswordException();
        }
        if (passwordHasher.matches(command.newPassword(), user.passwordHash())) {
            throw new UnchangedPasswordException();
        }

        // BCrypt (RNF004), through the same hasher as every other password.
        userRepository.updatePassword(user.id(), passwordHasher.hash(command.newPassword()));

        // Every session ends, this one too: the old password no longer opens any door.
        Instant now = clock.instant();
        refreshTokenRepository.revokeAllByUserId(user.id(), now);
        if (command.accessTokenId() != null && command.accessTokenExpiresAt() != null) {
            revokedAccessTokenRepository.revoke(command.accessTokenId(), command.accessTokenExpiresAt());
        }
    }
}
