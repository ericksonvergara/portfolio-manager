package com.portafolio_manager.backend.infrastructure.web.auth;

import com.portafolio_manager.backend.application.port.in.auth.AuthenticationResult;

import java.time.Instant;

record TokenResponse(
        String accessToken,
        String tokenType,
        Instant expiresAt
) {

    private static final String BEARER = "Bearer";

    static TokenResponse from(AuthenticationResult result) {
        return new TokenResponse(result.accessToken(), BEARER, result.accessTokenExpiresAt());
    }
}
