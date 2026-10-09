package com.portafolio_manager.backend.application.port.out.auth;

public interface PasswordHasherPort {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String passwordHash);
}
