package com.portafolio_manager.backend.application.port.out.project;

import com.portafolio_manager.backend.application.port.out.PersistencePort;
import com.portafolio_manager.backend.domain.project.ProjectSummary;

import java.util.List;

public interface ProjectRepositoryPort extends PersistencePort {

    /** Active projects only, featured first, then by display order and name. */
    List<ProjectSummary> findPublishedSummaries();
}
