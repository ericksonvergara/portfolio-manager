package com.portafolio_manager.backend.application.port.in.auth;

import java.time.Instant;

public record AuthenticationResult(
        String accessToken,
        Instant accessTokenExpiresAt,
        String refreshToken,
        Instant refreshTokenExpiresAt
) {
}
