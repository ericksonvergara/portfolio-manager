package com.portafolio_manager.backend.infrastructure.mail;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Objects;

/**
 * Password recovery (HU010): how long a link is valid (PASSWORD_RESET_TTL), the page of the
 * frontend it opens (PASSWORD_RESET_URL) and the sender of the email (MAIL_FROM).
 */
@ConfigurationProperties(prefix = "app.password-reset")
public record PasswordResetProperties(Duration ttl, String url, String mailFrom) {

    public PasswordResetProperties {
        Objects.requireNonNull(ttl, "app.password-reset.ttl must not be null");
        Objects.requireNonNull(url, "app.password-reset.url must not be null");
        Objects.requireNonNull(mailFrom, "app.password-reset.mail-from must not be null");
        if (ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("app.password-reset.ttl (PASSWORD_RESET_TTL) must be positive");
        }
    }
}
