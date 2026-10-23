package com.portafolio_manager.backend.application.port.in.auth;

/**
 * Takes an email and, if it belongs to an active user, sends a recovery link (HU010). It behaves
 * the same when the email is unknown, so it does not reveal which emails are registered.
 */
public interface RequestPasswordResetUseCase {

    void execute(String email);
}
