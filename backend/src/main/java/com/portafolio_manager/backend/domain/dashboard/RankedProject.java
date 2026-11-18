package com.portafolio_manager.backend.domain.dashboard;

import java.util.Objects;

/**
 * A project in the visits ranking (HU014). A hidden project keeps its place: its visits were real.
 */
public record RankedProject(Long id, String name, long visits, boolean published) {

    public RankedProject {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
    }
}
