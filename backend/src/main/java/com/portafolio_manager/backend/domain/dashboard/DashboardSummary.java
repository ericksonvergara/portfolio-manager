package com.portafolio_manager.backend.domain.dashboard;

import java.time.Instant;

/**
 * The general indicators of the portfolio (HU012). The latest project is null while there is none, and
 * the last access is null on the administrator's first session: it is the access before the current one.
 */
public record DashboardSummary(
        long totalProjects,
        long totalTechnologies,
        LatestProject latestProject,
        Instant lastAccessAt
) {
}
