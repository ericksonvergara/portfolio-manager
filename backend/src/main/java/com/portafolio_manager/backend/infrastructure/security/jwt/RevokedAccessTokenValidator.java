package com.portafolio_manager.backend.infrastructure.security.jwt;

import com.portafolio_manager.backend.application.port.out.auth.RevokedAccessTokenRepositoryPort;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Rejects tokens without a JWT ID and tokens revoked on logout.
 */
class RevokedAccessTokenValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error MISSING_ID =
            new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN, "The token has no JWT ID", null);
    private static final OAuth2Error REVOKED =
            new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN, "The token has been revoked", null);

    private final RevokedAccessTokenRepositoryPort revokedAccessTokenRepository;

    RevokedAccessTokenValidator(RevokedAccessTokenRepositoryPort revokedAccessTokenRepository) {
        this.revokedAccessTokenRepository = revokedAccessTokenRepository;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        String tokenId = jwt.getId();

        if (tokenId == null || tokenId.isBlank()) {
            return OAuth2TokenValidatorResult.failure(MISSING_ID);
        }

        if (revokedAccessTokenRepository.isRevoked(tokenId)) {
            return OAuth2TokenValidatorResult.failure(REVOKED);
        }

        return OAuth2TokenValidatorResult.success();
    }
}
