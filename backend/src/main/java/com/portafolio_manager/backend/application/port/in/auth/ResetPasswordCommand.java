package com.portafolio_manager.backend.application.port.in.auth;

public record ResetPasswordCommand(String token, String newPassword) {
}
