package com.portafolio_manager.backend.application.port.out.auth;

public interface RefreshTokenGeneratorPort {

    String generate();

    String hash(String rawToken);
}
