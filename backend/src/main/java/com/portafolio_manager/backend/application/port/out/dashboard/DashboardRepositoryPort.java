package com.portafolio_manager.backend.application.port.out.dashboard;

import com.portafolio_manager.backend.application.port.out.PersistencePort;
import com.portafolio_manager.backend.domain.dashboard.LatestProject;

import java.time.Instant;
import java.util.Optional;

public interface DashboardRepositoryPort extends PersistencePort {

    /** Every project, published or not. */
    long countProjects();

    /** The whole catalog, used by a project or not. */
    long countTechnologies();

    Optional<LatestProject> findLatestProject();

    /** The login before the current session of the user; empty on the first one. */
    Optional<Instant> findPreviousAccess(Long userId);
}
