package com.portafolio_manager.backend.domain.dashboard;

import java.util.Objects;

/**
 * The project whose detail page has the most visits.
 */
public record MostVisitedProject(String name, long visits) {

    public MostVisitedProject {
        Objects.requireNonNull(name, "name must not be null");
    }
}
