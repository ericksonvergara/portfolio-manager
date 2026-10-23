package com.portafolio_manager.backend.domain.auth;

/**
 * The recovery link is unknown, already used, replaced by a newer one or expired. All cases are
 * reported the same way.
 */
public class InvalidPasswordResetTokenException extends RuntimeException {

    public InvalidPasswordResetTokenException() {
        super("The password reset link is invalid or has expired");
    }
}
