package com.portafolio_manager.backend.domain.dashboard;

import java.time.Instant;
import java.util.Objects;

/**
 * The project published most recently among those still published.
 */
public record LatestPublishedProject(String name, Instant publishedAt) {

    public LatestPublishedProject {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(publishedAt, "publishedAt must not be null");
    }
}
