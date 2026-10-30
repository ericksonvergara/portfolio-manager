package com.portafolio_manager.backend.domain.contact;

public class ContactMessageNotFoundException extends RuntimeException {

    public ContactMessageNotFoundException(long id) {
        super("No contact message with id " + id);
    }
}
