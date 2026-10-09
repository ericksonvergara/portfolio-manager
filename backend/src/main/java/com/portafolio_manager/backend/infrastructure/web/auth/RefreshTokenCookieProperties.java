package com.portafolio_manager.backend.infrastructure.web.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.security.refresh-cookie")
public record RefreshTokenCookieProperties(
        @DefaultValue("refresh_token") String name,
        @DefaultValue("/api/auth") String path,
        @DefaultValue("true") boolean secure
) {
}
