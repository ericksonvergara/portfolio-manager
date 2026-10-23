package com.portafolio_manager.backend.application.port.in.technology;

import com.portafolio_manager.backend.application.port.in.UseCase;

/**
 * Takes the technology id and removes it from the catalog (HU021). Throws
 * {@code TechnologyNotFoundException} or, while a project uses it, {@code TechnologyInUseException}.
 */
public interface DeleteTechnologyUseCase extends UseCase<Long, Void> {
}
