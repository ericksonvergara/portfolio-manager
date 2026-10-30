package com.portafolio_manager.backend.domain.contact;

import java.util.Objects;

/**
 * A message sent from the contact form, with the key that identifies its sender for the rate limit
 * (the client IP address).
 */
public record ContactSubmission(ContactMessageDraft draft, String senderKey) {

    public ContactSubmission {
        Objects.requireNonNull(draft, "draft must not be null");
        Objects.requireNonNull(senderKey, "senderKey must not be null");
    }
}
