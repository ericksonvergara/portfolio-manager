package com.portafolio_manager.backend.application.port.in.technology;

import com.portafolio_manager.backend.application.port.in.UseCase;
import com.portafolio_manager.backend.domain.technology.AdminTechnology;

import java.util.List;

/**
 * Takes no input; returns the catalog with the number of projects that use each technology.
 */
public interface ListAdminTechnologiesUseCase extends UseCase<Void, List<AdminTechnology>> {
}
