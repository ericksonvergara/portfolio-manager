package com.portafolio_manager.backend.infrastructure.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(
        String secret,
        String issuer,
        Duration accessTokenTtl,
        Duration refreshTokenTtl
) {

    private static final int MIN_SECRET_BYTES = 32;

    public JwtProperties {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalArgumentException(
                    "app.security.jwt.secret (JWT_SECRET) must be at least " + MIN_SECRET_BYTES + " bytes long"
            );
        }
        Objects.requireNonNull(issuer, "app.security.jwt.issuer must not be null");
        Objects.requireNonNull(accessTokenTtl, "app.security.jwt.access-token-ttl must not be null");
        Objects.requireNonNull(refreshTokenTtl, "app.security.jwt.refresh-token-ttl must not be null");
    }
}
