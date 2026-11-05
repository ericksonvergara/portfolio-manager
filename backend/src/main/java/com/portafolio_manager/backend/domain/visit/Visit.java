package com.portafolio_manager.backend.domain.visit;

import java.time.Instant;
import java.util.Objects;

/**
 * One visit to the public site (HU025). The country and the project are optional: the country may be
 * unknown, and a visit to the landing page has no project.
 */
public record Visit(Instant visitedAt, String anonymizedIp, VisitorAgent agent, String country, Long projectId) {

    public Visit {
        Objects.requireNonNull(visitedAt, "visitedAt must not be null");
        Objects.requireNonNull(anonymizedIp, "anonymizedIp must not be null");
        Objects.requireNonNull(agent, "agent must not be null");
    }
}
