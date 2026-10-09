package com.portafolio_manager.backend.application.port.out.auth;

import com.portafolio_manager.backend.domain.user.User;

import java.util.Optional;

public interface AccessTokenProviderPort {

    AccessToken generate(User user);

    /**
     * @return the token details if it is well signed, not expired and not revoked; empty otherwise
     */
    Optional<AccessToken> parse(String token);
}
