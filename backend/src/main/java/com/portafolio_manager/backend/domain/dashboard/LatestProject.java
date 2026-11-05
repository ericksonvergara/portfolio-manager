package com.portafolio_manager.backend.domain.dashboard;

import java.time.Instant;
import java.util.Objects;

/**
 * The last project registered, published or not.
 */
public record LatestProject(String name, Instant registeredAt) {

    public LatestProject {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(registeredAt, "registeredAt must not be null");
    }
}
