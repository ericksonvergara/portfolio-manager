package com.portafolio_manager.backend.application.port.in.auth;

/**
 * Both tokens are optional: logout must succeed even if the client lost one of them.
 */
public record LogoutCommand(String refreshToken, String accessToken) {
}
