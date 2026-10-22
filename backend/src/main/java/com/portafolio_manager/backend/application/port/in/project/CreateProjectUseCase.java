package com.portafolio_manager.backend.application.port.in.project;

import com.portafolio_manager.backend.application.port.in.UseCase;
import com.portafolio_manager.backend.domain.project.AdminProject;
import com.portafolio_manager.backend.domain.project.ProjectDraft;

/**
 * Creates an inactive project (HU015): it can be published once it has a cover image (RN004).
 * Throws {@code DuplicateProjectNameException} (RN001) or {@code InvalidProjectException}.
 */
public interface CreateProjectUseCase extends UseCase<ProjectDraft, AdminProject> {
}
