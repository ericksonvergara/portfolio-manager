package com.portafolio_manager.backend.domain.project;

import java.util.Objects;

/**
 * Publishes ({@code active}) or hides a project without deleting it (HU018).
 */
public record ProjectPublication(Long id, boolean active) {

    public ProjectPublication {
        Objects.requireNonNull(id, "id must not be null");
    }
}
