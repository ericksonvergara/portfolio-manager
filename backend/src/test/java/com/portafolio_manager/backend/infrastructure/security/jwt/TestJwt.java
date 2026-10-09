package com.portafolio_manager.backend.infrastructure.security.jwt;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class TestJwt {

    public static final String SECRET = "test-only-jwt-secret-with-at-least-32-bytes";
    public static final String SECRET_PROPERTY = "app.security.jwt.secret=" + SECRET;
    public static final String ISSUER = "portfolio-manager";

    private TestJwt() {
    }

    public static String token(JwtEncoder encoder, String issuer, Instant expiresAt, String... roles) {
        return token(encoder, UUID.randomUUID().toString(), issuer, expiresAt, roles);
    }

    public static String token(JwtEncoder encoder, String tokenId, String issuer, Instant expiresAt, String... roles) {
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject("1")
                .issuedAt(expiresAt.minusSeconds(900))
                .expiresAt(expiresAt)
                .claim("roles", List.of(roles));

        if (tokenId != null) {
            claims.id(tokenId);
        }

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
    }
}
