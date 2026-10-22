package com.portafolio_manager.backend.application.port.in.project;

import com.portafolio_manager.backend.application.port.in.UseCase;
import com.portafolio_manager.backend.domain.project.AdminProject;
import com.portafolio_manager.backend.domain.project.GalleryImageRef;

/**
 * Removes an image and its file from the project gallery (HU019). Throws
 * {@code ProjectImageNotFoundException}.
 */
public interface RemoveGalleryImageUseCase extends UseCase<GalleryImageRef, AdminProject> {
}
