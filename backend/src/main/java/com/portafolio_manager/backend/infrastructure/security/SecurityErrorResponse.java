package com.portafolio_manager.backend.infrastructure.security;

import java.time.Instant;

record SecurityErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
