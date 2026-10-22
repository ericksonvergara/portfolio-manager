package com.portafolio_manager.backend.application.port.in.project;

import com.portafolio_manager.backend.application.port.in.UseCase;

/**
 * Deletes a project with its gallery and technology links (HU017). Throws
 * {@code ProjectNotFoundException} when it does not exist.
 */
public interface DeleteProjectUseCase extends UseCase<Long, Void> {
}
