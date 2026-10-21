package com.portafolio_manager.backend.domain.project;

/**
 * The project data breaks a business rule: missing fields, an invalid URL (RN002, RN003) or no
 * technology (RN005).
 */
public class InvalidProjectException extends RuntimeException {

    public InvalidProjectException(String message) {
        super(message);
    }
}
