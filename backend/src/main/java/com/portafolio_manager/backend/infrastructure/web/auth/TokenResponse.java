package com.portafolio_manager.backend.infrastructure.web.auth;

import com.portafolio_manager.backend.application.port.in.auth.AuthenticationResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Access token para enviar en la cabecera Authorization")
record TokenResponse(
        @Schema(description = "JWT firmado (HS256)", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(example = "Bearer")
        String tokenType,

        @Schema(description = "Instante de expiración del access token", example = "2026-10-09T12:15:00Z")
        Instant expiresAt
) {

    private static final String BEARER = "Bearer";

    static TokenResponse from(AuthenticationResult result) {
        return new TokenResponse(result.accessToken(), BEARER, result.accessTokenExpiresAt());
    }
}
