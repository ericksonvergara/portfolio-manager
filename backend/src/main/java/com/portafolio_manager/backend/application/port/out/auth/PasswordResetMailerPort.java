package com.portafolio_manager.backend.application.port.out.auth;

import java.time.Duration;

/**
 * Sends the password recovery link (HU010).
 */
public interface PasswordResetMailerPort {

    void sendPasswordReset(String email, String name, String resetLink, Duration validity);
}
