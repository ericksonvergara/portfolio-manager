package com.portafolio_manager.backend.application.port.in.auth;

import java.time.Instant;

/**
 * The signed-in user, the passwords, and the access token of the request (revoked once changed).
 */
public record ChangePasswordCommand(
        Long userId,
        String currentPassword,
        String newPassword,
        String accessTokenId,
        Instant accessTokenExpiresAt
) {
}
