package com.portafolio_manager.backend.application.port.in.project;

import com.portafolio_manager.backend.application.port.in.UseCase;
import com.portafolio_manager.backend.domain.project.ProjectSearch;
import com.portafolio_manager.backend.domain.project.ProjectSummary;

import java.util.List;

/**
 * Returns the active projects that match the search (HU004), featured first and then by display order (RN008).
 * Throws {@code InvalidProjectException} when the search is too long.
 */
public interface ListPublishedProjectsUseCase extends UseCase<ProjectSearch, List<ProjectSummary>> {
}
