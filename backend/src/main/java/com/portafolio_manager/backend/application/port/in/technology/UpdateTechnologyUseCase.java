package com.portafolio_manager.backend.application.port.in.technology;

import com.portafolio_manager.backend.application.port.in.UseCase;
import com.portafolio_manager.backend.domain.technology.AdminTechnology;
import com.portafolio_manager.backend.domain.technology.TechnologyUpdate;

/**
 * Edits a technology (HU021); the projects that use it show the change. Throws
 * {@code TechnologyNotFoundException} or {@code DuplicateTechnologyNameException}.
 */
public interface UpdateTechnologyUseCase extends UseCase<TechnologyUpdate, AdminTechnology> {
}
