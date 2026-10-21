package com.portafolio_manager.backend.domain.project;

import com.portafolio_manager.backend.domain.technology.Technology;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * A project as the admin panel sees it: active or not, with everything the form edits.
 */
public record AdminProject(
        Long id,
        String slug,
        String name,
        String shortDescription,
        String description,
        String coverImageUrl,
        String projectUrl,
        String repositoryUrl,
        String videoUrl,
        ProjectStatus status,
        LocalDate developedOn,
        int displayOrder,
        boolean featured,
        boolean active,
        List<Technology> technologies
) {

    public AdminProject {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(slug, "slug must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(status, "status must not be null");
        technologies = List.copyOf(Objects.requireNonNull(technologies, "technologies must not be null"));
    }
}
