package com.portafolio_manager.backend.application.port.in.auth;

/**
 * Sets a new password from a recovery link and closes every open session (HU010). Throws
 * {@code WeakPasswordException} or {@code InvalidPasswordResetTokenException}.
 */
public interface ResetPasswordUseCase {

    void execute(ResetPasswordCommand command);
}
