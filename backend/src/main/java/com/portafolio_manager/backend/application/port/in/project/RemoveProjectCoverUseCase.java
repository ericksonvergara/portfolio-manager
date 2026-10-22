package com.portafolio_manager.backend.application.port.in.project;

import com.portafolio_manager.backend.application.port.in.UseCase;
import com.portafolio_manager.backend.domain.project.AdminProject;

/**
 * Takes the project id and removes its cover image and file. Throws {@code ProjectNotFoundException}
 * or, for an active project, {@code ProjectNotPublishableException} (RN004).
 */
public interface RemoveProjectCoverUseCase extends UseCase<Long, AdminProject> {
}
