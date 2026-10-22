package com.portafolio_manager.backend.application.port.in.project;

import com.portafolio_manager.backend.application.port.in.UseCase;
import com.portafolio_manager.backend.domain.project.AdminProject;

import java.util.List;

/**
 * Takes no input; returns every project, active or not, by display order and then by name.
 */
public interface ListAdminProjectsUseCase extends UseCase<Void, List<AdminProject>> {
}
