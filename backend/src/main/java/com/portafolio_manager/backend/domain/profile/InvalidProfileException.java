package com.portafolio_manager.backend.domain.profile;

public class InvalidProfileException extends RuntimeException {

    public InvalidProfileException(String message) {
        super(message);
    }
}
