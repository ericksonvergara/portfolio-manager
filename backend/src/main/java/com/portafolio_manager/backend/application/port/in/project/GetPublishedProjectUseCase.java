package com.portafolio_manager.backend.application.port.in.project;

import com.portafolio_manager.backend.application.port.in.UseCase;
import com.portafolio_manager.backend.domain.project.ProjectDetail;

/**
 * Takes the project slug; throws {@code ProjectNotFoundException} when no active project has it.
 */
public interface GetPublishedProjectUseCase extends UseCase<String, ProjectDetail> {
}
