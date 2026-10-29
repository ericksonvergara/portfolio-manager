package com.portafolio_manager.backend.application.port.out.contact;

/**
 * Limits how many messages the same sender can send in a time window (anti-spam, HU006).
 */
public interface ContactRateLimiterPort {

    /** Counts one more message for the sender; false if it is over the limit. */
    boolean tryAcquire(String senderKey);
}
