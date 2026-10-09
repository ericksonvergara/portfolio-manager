package com.portafolio_manager.backend.domain.user;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public record User(
        Long id,
        String name,
        String email,
        String passwordHash,
        Set<Role> roles,
        boolean active,
        Instant lastLoginAt
) {

    public User {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(passwordHash, "passwordHash must not be null");
        email = normalizeEmail(email);
        roles = Set.copyOf(Objects.requireNonNull(roles, "roles must not be null"));
    }

    public static User newUser(String name, String email, String passwordHash, Set<Role> roles) {
        return new User(null, name, email, passwordHash, roles, true, null);
    }

    public static String normalizeEmail(String email) {
        Objects.requireNonNull(email, "email must not be null");
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
