package com.portafolio_manager.backend.application.port.in.project;

import com.portafolio_manager.backend.application.port.in.UseCase;
import com.portafolio_manager.backend.domain.project.ProjectSummary;

import java.util.List;

/**
 * Takes no input; returns the active projects, featured first and then by display order (RN008).
 */
public interface ListPublishedProjectsUseCase extends UseCase<Void, List<ProjectSummary>> {
}
