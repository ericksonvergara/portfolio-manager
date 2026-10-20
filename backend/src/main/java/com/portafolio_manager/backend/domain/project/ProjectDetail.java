package com.portafolio_manager.backend.domain.project;

import com.portafolio_manager.backend.domain.technology.Technology;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Everything the project detail page shows (HU003). The URLs, the video and the date are optional.
 */
public record ProjectDetail(
        String slug,
        String name,
        String description,
        String coverImageUrl,
        String projectUrl,
        String repositoryUrl,
        String videoUrl,
        ProjectStatus status,
        LocalDate developedOn,
        List<ProjectImage> gallery,
        List<Technology> technologies
) {

    public ProjectDetail {
        Objects.requireNonNull(slug, "slug must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(description, "description must not be null");
        Objects.requireNonNull(status, "status must not be null");
        gallery = List.copyOf(Objects.requireNonNull(gallery, "gallery must not be null"));
        technologies = List.copyOf(Objects.requireNonNull(technologies, "technologies must not be null"));
    }
}
