package com.portafolio_manager.backend.domain.technology;

import java.util.Objects;

/**
 * The data the administrator enters for a technology of the catalog (HU021).
 */
public record TechnologyDraft(String name, TechnologyCategory category, int displayOrder) {

    public TechnologyDraft {
        if (name == null || name.isBlank()) {
            throw new InvalidTechnologyException("name must not be blank");
        }
        name = name.strip();
        if (name.length() > 50) {
            throw new InvalidTechnologyException("name must be at most 50 characters");
        }
        Objects.requireNonNull(category, "category must not be null");
        if (displayOrder < 0) {
            throw new InvalidTechnologyException("displayOrder must not be negative");
        }
    }
}
