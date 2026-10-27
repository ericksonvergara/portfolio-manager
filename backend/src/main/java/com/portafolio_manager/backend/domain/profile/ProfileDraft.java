package com.portafolio_manager.backend.domain.profile;

import com.portafolio_manager.backend.domain.WebUrl;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The data the administrator enters for the public profile (HU023). The email is the public contact
 * address, not the login one. The location is optional and the photo is managed apart, so a draft never
 * carries it. The social links keep the order in which they are given, one per platform.
 */
public record ProfileDraft(
        String fullName,
        String headline,
        String about,
        String email,
        String location,
        List<SocialLink> socialLinks
) {

    /** Something before the @, a domain with at least one dot, and no spaces. */
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s.]+$");

    public ProfileDraft {
        fullName = required(fullName, "fullName", 100);
        headline = required(headline, "headline", 150);
        about = required(about, "about", 2000);
        email = required(email, "email", 150);
        if (!EMAIL.matcher(email).matches()) {
            throw new InvalidProfileException("email must be a valid email address");
        }
        location = optional(location, "location", 100);
        socialLinks = List.copyOf(Objects.requireNonNull(socialLinks, "socialLinks must not be null"));
        Set<SocialPlatform> platforms = EnumSet.noneOf(SocialPlatform.class);
        for (SocialLink link : socialLinks) {
            if (!platforms.add(link.platform())) {
                throw new InvalidProfileException("Only one link per platform is allowed: " + link.platform());
            }
            if (link.url().length() > 500 || !WebUrl.isValid(link.url())) {
                throw new InvalidProfileException(link.platform() + " link must be a valid http or https URL");
            }
        }
    }

    private static String required(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new InvalidProfileException(field + " must not be blank");
        }
        return checkLength(value.strip(), field, maxLength);
    }

    /** Blank means "not set". */
    private static String optional(String value, String field, int maxLength) {
        return value == null || value.isBlank() ? null : checkLength(value.strip(), field, maxLength);
    }

    private static String checkLength(String value, String field, int maxLength) {
        if (value.length() > maxLength) {
            throw new InvalidProfileException(field + " must be at most " + maxLength + " characters");
        }
        return value;
    }
}
