package com.portafolio_manager.backend.domain.contact;

/**
 * The same sender reached the limit of messages for the time window (anti-spam, HU006).
 */
public class TooManyContactMessagesException extends RuntimeException {

    public TooManyContactMessagesException() {
        super("Too many messages. Please try again later");
    }
}
