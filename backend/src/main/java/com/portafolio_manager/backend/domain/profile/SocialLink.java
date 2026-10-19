package com.portafolio_manager.backend.domain.profile;

import java.util.Objects;

public record SocialLink(SocialPlatform platform, String url) {

    public SocialLink {
        Objects.requireNonNull(platform, "platform must not be null");
        Objects.requireNonNull(url, "url must not be null");
    }
}
