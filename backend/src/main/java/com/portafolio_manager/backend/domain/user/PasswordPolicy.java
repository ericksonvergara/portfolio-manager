package com.portafolio_manager.backend.domain.user;

import java.util.regex.Pattern;

/**
 * Password rules from RF004: 6-12 characters with at least one uppercase letter,
 * one lowercase letter, one digit and one special character.
 */
public final class PasswordPolicy {

    public static final String DESCRIPTION =
            "6-12 characters with at least one uppercase letter, one lowercase letter, one digit and one special character";

    private static final Pattern PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{6,12}$"
    );

    private PasswordPolicy() {
    }

    public static boolean isSatisfiedBy(String rawPassword) {
        return rawPassword != null && PATTERN.matcher(rawPassword).matches();
    }
}
