package com.portafolio_manager.backend.domain.contact;

import com.portafolio_manager.backend.domain.EmailAddress;

/**
 * What a visitor writes in the contact form (HU006): every field is required.
 */
public record ContactMessageDraft(String name, String email, String message) {

    public ContactMessageDraft {
        name = required(name, "name", 100);
        email = required(email, "email", 150);
        if (!EmailAddress.isValid(email)) {
            throw new InvalidContactMessageException("email must be a valid email address");
        }
        message = required(message, "message", 2000);
    }

    private static String required(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new InvalidContactMessageException(field + " must not be blank");
        }
        String stripped = value.strip();
        if (stripped.length() > maxLength) {
            throw new InvalidContactMessageException(field + " must be at most " + maxLength + " characters");
        }
        return stripped;
    }
}
