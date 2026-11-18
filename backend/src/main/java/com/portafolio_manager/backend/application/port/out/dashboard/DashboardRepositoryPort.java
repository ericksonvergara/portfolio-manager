package com.portafolio_manager.backend.application.port.out.dashboard;

import com.portafolio_manager.backend.application.port.out.PersistencePort;
import com.portafolio_manager.backend.domain.dashboard.LatestProject;
import com.portafolio_manager.backend.domain.dashboard.LatestPublishedProject;
import com.portafolio_manager.backend.domain.dashboard.MostVisitedProject;

import java.time.Instant;
import java.util.Optional;

public interface DashboardRepositoryPort extends PersistencePort {

    /** Every project, published or not. */
    long countProjects();

    /** The whole catalog, used by a project or not. */
    long countTechnologies();

    Optional<LatestProject> findLatestProject();

    /** Among the projects published now: one that was hidden afterwards does not count. */
    Optional<LatestPublishedProject> findLatestPublishedProject();

    /** The login before the current session of the user; empty on the first one. */
    Optional<Instant> findPreviousAccess(Long userId);

    long countVisits();

    long countVisitsSince(Instant start);

    /** Among the existing projects, with ties broken by name. */
    Optional<MostVisitedProject> findMostVisitedProject();
}
