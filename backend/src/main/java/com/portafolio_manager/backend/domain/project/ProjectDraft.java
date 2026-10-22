package com.portafolio_manager.backend.domain.project;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

/**
 * The data the administrator enters for a project (HU015). The URLs and the date are optional;
 * the images are managed apart (HU019), so a draft never carries them.
 */
public record ProjectDraft(
        String name,
        String shortDescription,
        String description,
        String projectUrl,
        String repositoryUrl,
        String videoUrl,
        ProjectStatus status,
        LocalDate developedOn,
        int displayOrder,
        boolean featured,
        Set<Long> technologyIds
) {

    public ProjectDraft {
        name = required(name, "name");
        shortDescription = required(shortDescription, "shortDescription");
        description = required(description, "description");
        projectUrl = webUrl(projectUrl, "projectUrl");          // RN002
        repositoryUrl = webUrl(repositoryUrl, "repositoryUrl"); // RN003
        videoUrl = webUrl(videoUrl, "videoUrl");
        Objects.requireNonNull(status, "status must not be null");
        if (displayOrder < 0) {
            throw new InvalidProjectException("displayOrder must not be negative");
        }
        technologyIds = Set.copyOf(Objects.requireNonNull(technologyIds, "technologyIds must not be null"));
        if (technologyIds.isEmpty()) {
            throw new InvalidProjectException("A project needs at least one technology"); // RN005
        }
    }

    public String slug() {
        return ProjectSlug.from(name);
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new InvalidProjectException(field + " must not be blank");
        }
        return value.strip();
    }

    /** Blank means "not set"; anything else must be an absolute http(s) URL with a host. */
    private static String webUrl(String value, String field) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String url = value.strip();
        try {
            URI uri = new URI(url);
            boolean web = "http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme());
            if (web && uri.getHost() != null) {
                return url;
            }
        } catch (URISyntaxException ignored) {
            // reported below
        }
        throw new InvalidProjectException(field + " must be a valid http or https URL");
    }
}
