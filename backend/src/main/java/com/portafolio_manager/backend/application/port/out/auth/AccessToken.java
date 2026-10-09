package com.portafolio_manager.backend.application.port.out.auth;

import java.time.Instant;

public record AccessToken(String value, String id, Instant expiresAt) {
}
