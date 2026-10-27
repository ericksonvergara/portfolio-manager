package com.portafolio_manager.backend.domain.technology;

import java.util.Objects;

public record TechnologyUpdate(Long id, TechnologyDraft draft) {

    public TechnologyUpdate {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(draft, "draft must not be null");
    }
}
