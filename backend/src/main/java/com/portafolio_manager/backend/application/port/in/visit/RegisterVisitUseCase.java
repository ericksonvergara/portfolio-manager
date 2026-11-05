package com.portafolio_manager.backend.application.port.in.visit;

import com.portafolio_manager.backend.application.port.in.UseCase;
import com.portafolio_manager.backend.domain.visit.VisitRequest;

/**
 * Registers a visit to the public site (HU025). Throws {@code ProjectNotFoundException} when the slug
 * is not a published project.
 */
public interface RegisterVisitUseCase extends UseCase<VisitRequest, Void> {
}
