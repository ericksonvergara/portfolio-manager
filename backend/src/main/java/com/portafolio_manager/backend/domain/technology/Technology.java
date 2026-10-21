package com.portafolio_manager.backend.domain.technology;

import java.util.Objects;

public record Technology(Long id, String name, TechnologyCategory category) {

    public Technology {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(category, "category must not be null");
    }
}
