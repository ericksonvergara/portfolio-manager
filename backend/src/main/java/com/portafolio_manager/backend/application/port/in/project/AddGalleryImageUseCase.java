package com.portafolio_manager.backend.application.port.in.project;

import com.portafolio_manager.backend.application.port.in.UseCase;
import com.portafolio_manager.backend.domain.project.AdminProject;
import com.portafolio_manager.backend.domain.project.ImageChange;

/**
 * Adds an image with its alternative text at the end of the project gallery (HU019). Throws
 * {@code ProjectNotFoundException} or {@code InvalidImageException}.
 */
public interface AddGalleryImageUseCase extends UseCase<ImageChange, AdminProject> {
}
