package com.portafolio_manager.backend.infrastructure.config;

import com.portafolio_manager.backend.application.port.in.auth.LoginUseCase;
import com.portafolio_manager.backend.application.port.in.auth.LogoutUseCase;
import com.portafolio_manager.backend.application.port.in.auth.RefreshSessionUseCase;
import com.portafolio_manager.backend.application.port.out.auth.AccessTokenProviderPort;
import com.portafolio_manager.backend.application.port.out.auth.PasswordHasherPort;
import com.portafolio_manager.backend.application.port.out.auth.RefreshTokenGeneratorPort;
import com.portafolio_manager.backend.application.port.out.auth.RefreshTokenRepositoryPort;
import com.portafolio_manager.backend.application.port.out.auth.RevokedAccessTokenRepositoryPort;
import com.portafolio_manager.backend.application.port.out.user.UserRepositoryPort;
import com.portafolio_manager.backend.application.service.auth.LoginService;
import com.portafolio_manager.backend.application.service.auth.LogoutService;
import com.portafolio_manager.backend.application.service.auth.RefreshSessionService;
import com.portafolio_manager.backend.application.service.auth.SessionIssuer;
import com.portafolio_manager.backend.infrastructure.security.jwt.JwtProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Wires the framework-free application services as Spring beans.
 */
@Configuration
public class AuthenticationConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    SessionIssuer sessionIssuer(
            AccessTokenProviderPort accessTokenProvider,
            RefreshTokenGeneratorPort refreshTokenGenerator,
            RefreshTokenRepositoryPort refreshTokenRepository,
            JwtProperties jwtProperties
    ) {
        return new SessionIssuer(
                accessTokenProvider,
                refreshTokenGenerator,
                refreshTokenRepository,
                jwtProperties.refreshTokenTtl()
        );
    }

    @Bean
    LoginUseCase loginUseCase(
            UserRepositoryPort userRepository,
            PasswordHasherPort passwordHasher,
            SessionIssuer sessionIssuer,
            Clock clock
    ) {
        return new LoginService(userRepository, passwordHasher, sessionIssuer, clock);
    }

    @Bean
    RefreshSessionUseCase refreshSessionUseCase(
            RefreshTokenRepositoryPort refreshTokenRepository,
            RefreshTokenGeneratorPort refreshTokenGenerator,
            UserRepositoryPort userRepository,
            SessionIssuer sessionIssuer,
            Clock clock
    ) {
        return new RefreshSessionService(
                refreshTokenRepository,
                refreshTokenGenerator,
                userRepository,
                sessionIssuer,
                clock
        );
    }

    @Bean
    LogoutUseCase logoutUseCase(
            RefreshTokenRepositoryPort refreshTokenRepository,
            RefreshTokenGeneratorPort refreshTokenGenerator,
            AccessTokenProviderPort accessTokenProvider,
            RevokedAccessTokenRepositoryPort revokedAccessTokenRepository,
            Clock clock
    ) {
        return new LogoutService(
                refreshTokenRepository,
                refreshTokenGenerator,
                accessTokenProvider,
                revokedAccessTokenRepository,
                clock
        );
    }
}
