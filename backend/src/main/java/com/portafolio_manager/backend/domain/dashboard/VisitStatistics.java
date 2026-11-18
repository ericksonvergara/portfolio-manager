package com.portafolio_manager.backend.domain.dashboard;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The visits to the public site (HU013) and the projects with the most visits to their detail, the most
 * visited first (HU014). The ranking is empty while no detail has visits.
 */
public record VisitStatistics(long total, long today, long thisMonth, List<RankedProject> ranking) {

    public VisitStatistics {
        ranking = List.copyOf(Objects.requireNonNull(ranking, "ranking must not be null"));
    }

    public Optional<RankedProject> mostVisitedProject() {
        return ranking.stream().findFirst();
    }
}
