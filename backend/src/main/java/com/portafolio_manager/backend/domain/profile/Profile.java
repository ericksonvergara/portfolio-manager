package com.portafolio_manager.backend.domain.profile;

import java.util.List;
import java.util.Objects;

/**
 * Public profile of the portfolio owner. The photo and the location are optional.
 */
public record Profile(
        String fullName,
        String headline,
        String about,
        String photoUrl,
        String email,
        String location,
        List<SocialLink> socialLinks
) {

    public Profile {
        Objects.requireNonNull(fullName, "fullName must not be null");
        Objects.requireNonNull(headline, "headline must not be null");
        Objects.requireNonNull(about, "about must not be null");
        Objects.requireNonNull(email, "email must not be null");
        socialLinks = List.copyOf(Objects.requireNonNull(socialLinks, "socialLinks must not be null"));
    }
}
