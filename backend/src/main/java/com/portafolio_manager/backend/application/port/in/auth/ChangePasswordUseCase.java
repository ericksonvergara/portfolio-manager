package com.portafolio_manager.backend.application.port.in.auth;

/**
 * Changes the password of the signed-in user and closes every session, this one included (HU011).
 * Throws {@code IncorrectPasswordException}, {@code WeakPasswordException} or
 * {@code UnchangedPasswordException}.
 */
public interface ChangePasswordUseCase {

    void execute(ChangePasswordCommand command);
}
