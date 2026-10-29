package com.portafolio_manager.backend.domain.contact;

import java.time.Instant;
import java.util.Objects;

/**
 * A message received from the contact form, as the administrator reads it in the inbox (HU006).
 * It stays unread until the administrator marks it.
 */
public record ContactMessage(Long id, String name, String email, String message, Instant receivedAt, Instant readAt) {

    public ContactMessage {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(email, "email must not be null");
        Objects.requireNonNull(message, "message must not be null");
        Objects.requireNonNull(receivedAt, "receivedAt must not be null");
    }

    public boolean read() {
        return readAt != null;
    }
}
