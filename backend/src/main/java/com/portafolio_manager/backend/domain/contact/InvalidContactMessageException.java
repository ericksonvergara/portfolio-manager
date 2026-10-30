package com.portafolio_manager.backend.domain.contact;

public class InvalidContactMessageException extends RuntimeException {

    public InvalidContactMessageException(String message) {
        super(message);
    }
}
