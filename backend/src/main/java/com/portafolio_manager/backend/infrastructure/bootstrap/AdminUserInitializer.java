package com.portafolio_manager.backend.infrastructure.bootstrap;

import com.portafolio_manager.backend.application.port.out.auth.PasswordHasherPort;
import com.portafolio_manager.backend.application.port.out.user.UserRepositoryPort;
import com.portafolio_manager.backend.domain.user.PasswordPolicy;
import com.portafolio_manager.backend.domain.user.Role;
import com.portafolio_manager.backend.domain.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Creates the initial administrator from ADMIN_EMAIL / ADMIN_PASSWORD on first start-up,
 * so no credentials live in the source code or in migrations.
 */
@Component
@EnableConfigurationProperties(AdminBootstrapProperties.class)
class AdminUserInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserInitializer.class);

    private final AdminBootstrapProperties properties;
    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;

    AdminUserInitializer(
            AdminBootstrapProperties properties,
            UserRepositoryPort userRepository,
            PasswordHasherPort passwordHasher
    ) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.isConfigured()) {
            logger.warn("ADMIN_EMAIL/ADMIN_PASSWORD not set: skipping initial administrator creation");
            return;
        }

        String email = User.normalizeEmail(properties.email());
        if (userRepository.existsByEmail(email)) {
            return;
        }

        if (!PasswordPolicy.isSatisfiedBy(properties.password())) {
            throw new IllegalStateException("ADMIN_PASSWORD must have " + PasswordPolicy.DESCRIPTION);
        }

        userRepository.save(User.newUser(
                properties.name(),
                email,
                passwordHasher.hash(properties.password()),
                Set.of(Role.ADMIN)
        ));
        logger.info("Initial administrator created: {}", email);
    }
}
