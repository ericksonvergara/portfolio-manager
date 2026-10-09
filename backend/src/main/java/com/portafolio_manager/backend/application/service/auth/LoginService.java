package com.portafolio_manager.backend.application.service.auth;

import com.portafolio_manager.backend.application.port.in.auth.AuthenticationResult;
import com.portafolio_manager.backend.application.port.in.auth.LoginCommand;
import com.portafolio_manager.backend.application.port.in.auth.LoginUseCase;
import com.portafolio_manager.backend.application.port.out.auth.PasswordHasherPort;
import com.portafolio_manager.backend.application.port.out.user.UserRepositoryPort;
import com.portafolio_manager.backend.domain.auth.InvalidCredentialsException;
import com.portafolio_manager.backend.domain.user.User;

import java.time.Clock;
import java.time.Instant;

public class LoginService implements LoginUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;
    private final SessionIssuer sessionIssuer;
    private final Clock clock;

    public LoginService(
            UserRepositoryPort userRepository,
            PasswordHasherPort passwordHasher,
            SessionIssuer sessionIssuer,
            Clock clock
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.sessionIssuer = sessionIssuer;
        this.clock = clock;
    }

    @Override
    public AuthenticationResult execute(LoginCommand command) {
        User user = userRepository.findByEmail(User.normalizeEmail(command.email()))
                .filter(User::active)
                .filter(candidate -> passwordHasher.matches(command.password(), candidate.passwordHash()))
                .orElseThrow(InvalidCredentialsException::new);

        Instant now = clock.instant();
        userRepository.updateLastLogin(user.id(), now);

        return sessionIssuer.issue(user, now);
    }
}
