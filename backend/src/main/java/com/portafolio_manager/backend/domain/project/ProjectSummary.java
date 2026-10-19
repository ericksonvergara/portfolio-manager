package com.portafolio_manager.backend.domain.project;

import com.portafolio_manager.backend.domain.technology.Technology;

import java.util.List;
import java.util.Objects;

/**
 * What a project card on the landing page shows. The project and repository URLs are optional.
 */
public record ProjectSummary(
        String slug,
        String name,
        String shortDescription,
        String coverImageUrl,
        String projectUrl,
        String repositoryUrl,
        boolean featured,
        List<Technology> technologies
) {

    public ProjectSummary {
        Objects.requireNonNull(slug, "slug must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(shortDescription, "shortDescription must not be null");
        technologies = List.copyOf(Objects.requireNonNull(technologies, "technologies must not be null"));
    }
}
