package com.portafolio_manager.backend.infrastructure.contact;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Objects;

/**
 * Contact form settings (HU006): how many messages one sender may send per window, and the sender of
 * the notice email.
 */
@ConfigurationProperties(prefix = "app.contact")
public record ContactProperties(int maxMessages, Duration window, String mailFrom) {

    public ContactProperties {
        Objects.requireNonNull(window, "app.contact.window must not be null");
        Objects.requireNonNull(mailFrom, "app.contact.mail-from must not be null");
        if (maxMessages < 1) {
            throw new IllegalArgumentException("app.contact.max-messages (CONTACT_MAX_MESSAGES) must be at least 1");
        }
        if (window.isZero() || window.isNegative()) {
            throw new IllegalArgumentException("app.contact.window (CONTACT_WINDOW) must be positive");
        }
    }
}
