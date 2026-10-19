package com.portafolio_manager.backend.application.port.in.technology;

import com.portafolio_manager.backend.application.port.in.UseCase;
import com.portafolio_manager.backend.domain.technology.Technology;

import java.util.List;

/**
 * Takes no input; returns the catalog in display order.
 */
public interface ListTechnologiesUseCase extends UseCase<Void, List<Technology>> {
}
