package com.portafolio_manager.backend.application.port.in.project;

import com.portafolio_manager.backend.application.port.in.UseCase;
import com.portafolio_manager.backend.domain.project.AdminProject;
import com.portafolio_manager.backend.domain.project.ImageChange;

/**
 * Uploads or replaces the cover image of a project (HU019); the replaced file is deleted. Throws
 * {@code ProjectNotFoundException} or {@code InvalidImageException}.
 */
public interface SetProjectCoverUseCase extends UseCase<ImageChange, AdminProject> {
}
