package com.portafolio_manager.backend.application.port.in.technology;

import com.portafolio_manager.backend.application.port.in.UseCase;
import com.portafolio_manager.backend.domain.technology.AdminTechnology;
import com.portafolio_manager.backend.domain.technology.TechnologyDraft;

/**
 * Adds a technology to the catalog (HU021). Throws {@code DuplicateTechnologyNameException}.
 */
public interface CreateTechnologyUseCase extends UseCase<TechnologyDraft, AdminTechnology> {
}
