package com.portafolio_manager.backend.domain.user;

/**
 * The new password is the current one (HU011).
 */
public class UnchangedPasswordException extends RuntimeException {

    public UnchangedPasswordException() {
        super("The new password must be different from the current one");
    }
}
