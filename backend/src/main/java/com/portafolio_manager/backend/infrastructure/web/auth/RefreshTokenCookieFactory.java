package com.portafolio_manager.backend.infrastructure.web.auth;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * The refresh token travels only in an HttpOnly, SameSite=Strict cookie scoped to the auth endpoints,
 * so JavaScript cannot read it and browsers do not send it anywhere else.
 */
@Component
@EnableConfigurationProperties(RefreshTokenCookieProperties.class)
class RefreshTokenCookieFactory {

    private static final String SAME_SITE = "Strict";

    private final RefreshTokenCookieProperties properties;
    private final Clock clock;

    RefreshTokenCookieFactory(RefreshTokenCookieProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    String cookieName() {
        return properties.name();
    }

    ResponseCookie create(String refreshToken, Instant expiresAt) {
        Duration maxAge = Duration.between(clock.instant(), expiresAt);
        return baseCookie(refreshToken).maxAge(maxAge).build();
    }

    ResponseCookie clear() {
        return baseCookie("").maxAge(Duration.ZERO).build();
    }

    private ResponseCookie.ResponseCookieBuilder baseCookie(String value) {
        return ResponseCookie.from(properties.name(), value)
                .httpOnly(true)
                .secure(properties.secure())
                .sameSite(SAME_SITE)
                .path(properties.path());
    }
}
