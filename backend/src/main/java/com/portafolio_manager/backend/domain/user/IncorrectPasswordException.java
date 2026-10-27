package com.portafolio_manager.backend.domain.user;

/**
 * The current password given to change it is wrong (HU011).
 */
public class IncorrectPasswordException extends RuntimeException {

    public IncorrectPasswordException() {
        super("The current password is incorrect");
    }
}
