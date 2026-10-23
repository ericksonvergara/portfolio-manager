package com.portafolio_manager.backend.domain.technology;

import java.util.Objects;

/**
 * A technology as the admin panel sees it, with the number of projects that use it.
 */
public record AdminTechnology(Long id, String name, TechnologyCategory category, int displayOrder, long projectCount) {

    public AdminTechnology {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(category, "category must not be null");
    }
}
