package com.portafolio_manager.backend.domain;

import java.util.regex.Pattern;

/**
 * The email addresses the portfolio accepts: something before the @, a domain with at least one dot,
 * and no spaces.
 */
public final class EmailAddress {

    private static final Pattern FORMAT = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s.]+$");

    private EmailAddress() {
    }

    public static boolean isValid(String email) {
        return FORMAT.matcher(email).matches();
    }
}
