package com.portafolio_manager.backend.domain.dashboard;

import java.time.Instant;

/**
 * The indicators of the portfolio (HU012) and its visits (HU013). The latest projects are null while
 * there is none, and the last access is null on the administrator's first session: it is the access
 * before the current one.
 */
public record DashboardSummary(
        long totalProjects,
        long totalTechnologies,
        LatestProject latestProject,
        LatestPublishedProject latestPublishedProject,
        Instant lastAccessAt,
        VisitStatistics visits
) {
}
