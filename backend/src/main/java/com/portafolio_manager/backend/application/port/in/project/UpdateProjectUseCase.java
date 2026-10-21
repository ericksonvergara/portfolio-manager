package com.portafolio_manager.backend.application.port.in.project;

import com.portafolio_manager.backend.application.port.in.UseCase;
import com.portafolio_manager.backend.domain.project.AdminProject;
import com.portafolio_manager.backend.domain.project.ProjectUpdate;

/**
 * Replaces the data of a project (HU016) and regenerates its slug from the name. Keeps its
 * publication and images. Throws {@code ProjectNotFoundException}, {@code DuplicateProjectNameException}
 * (RN001) or {@code InvalidProjectException}.
 */
public interface UpdateProjectUseCase extends UseCase<ProjectUpdate, AdminProject> {
}
