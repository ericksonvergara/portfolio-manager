package com.portafolio_manager.backend.application.port.in.project;

import com.portafolio_manager.backend.application.port.in.UseCase;
import com.portafolio_manager.backend.domain.project.AdminProject;

/**
 * Takes the project id, active or not; throws {@code ProjectNotFoundException} when it does not exist.
 */
public interface GetAdminProjectUseCase extends UseCase<Long, AdminProject> {
}
