package com.portafolio_manager.backend.infrastructure.security.jwt;

import com.portafolio_manager.backend.application.port.out.auth.AccessToken;
import com.portafolio_manager.backend.application.port.out.auth.AccessTokenProviderPort;
import com.portafolio_manager.backend.domain.user.Role;
import com.portafolio_manager.backend.domain.user.User;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
class JwtAccessTokenProvider implements AccessTokenProviderPort {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final JwtProperties properties;
    private final Clock clock;

    JwtAccessTokenProvider(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, JwtProperties properties, Clock clock) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public AccessToken generate(User user) {
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(properties.accessTokenTtl());
        List<String> roles = user.roles().stream().map(Role::name).sorted().toList();

        String tokenId = UUID.randomUUID().toString();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .id(tokenId)
                .issuer(properties.issuer())
                .subject(user.id().toString())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("email", user.email())
                .claim("name", user.name())
                .claim(JwtConfig.ROLES_CLAIM, roles)
                .build();

        JwsHeader header = JwsHeader.with(JwtConfig.SIGNING_ALGORITHM).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new AccessToken(token, tokenId, expiresAt);
    }

    @Override
    public Optional<AccessToken> parse(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            return Optional.of(new AccessToken(token, jwt.getId(), jwt.getExpiresAt()));
        } catch (JwtException exception) {
            return Optional.empty();
        }
    }
}
