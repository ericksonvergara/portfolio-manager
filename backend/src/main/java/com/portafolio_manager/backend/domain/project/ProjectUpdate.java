package com.portafolio_manager.backend.domain.project;

import java.util.Objects;

/**
 * New data for an existing project (HU016). Publication and images are not part of it.
 */
public record ProjectUpdate(Long id, ProjectDraft draft) {

    public ProjectUpdate {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(draft, "draft must not be null");
    }
}
