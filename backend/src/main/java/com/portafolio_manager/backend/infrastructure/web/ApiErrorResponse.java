package com.portafolio_manager.backend.infrastructure.web;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Schema(description = "Formato común de los errores de la API")
public record ApiErrorResponse(
        @Schema(example = "2026-10-09T12:00:00Z")
        Instant timestamp,

        @Schema(example = "401")
        int status,

        @Schema(example = "Unauthorized")
        String error,

        @Schema(example = "Invalid email or password")
        String message,

        @Schema(example = "/api/auth/login")
        String path
) {

    public static ApiErrorResponse of(HttpStatus status, String message, String path) {
        return new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        );
    }
}
