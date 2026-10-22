package com.portafolio_manager.backend.application.port.in.project;

import com.portafolio_manager.backend.application.port.in.UseCase;
import com.portafolio_manager.backend.domain.project.AdminProject;
import com.portafolio_manager.backend.domain.project.ProjectPublication;

/**
 * Activates or deactivates a project (HU018); only active projects appear on the landing page
 * (RN008). Throws {@code ProjectNotFoundException} or, when activating a project without a cover
 * image, {@code ProjectNotPublishableException} (RN004).
 */
public interface SetProjectPublicationUseCase extends UseCase<ProjectPublication, AdminProject> {
}
