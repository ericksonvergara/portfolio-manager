package com.portafolio_manager.backend.domain.user;

/**
 * The new password does not follow the password policy (RF004).
 */
public class WeakPasswordException extends RuntimeException {

    public WeakPasswordException() {
        super("The password must have " + PasswordPolicy.DESCRIPTION);
    }
}
