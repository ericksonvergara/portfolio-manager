package com.portafolio_manager.backend.application.service.project;

import com.portafolio_manager.backend.application.port.in.project.GetAdminProjectUseCase;
import com.portafolio_manager.backend.application.port.out.project.ProjectRepositoryPort;
import com.portafolio_manager.backend.domain.project.AdminProject;
import com.portafolio_manager.backend.domain.project.ProjectNotFoundException;

public class GetAdminProjectService implements GetAdminProjectUseCase {

    private final ProjectRepositoryPort projectRepository;

    public GetAdminProjectService(ProjectRepositoryPort projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public AdminProject execute(Long id) {
        return projectRepository.findForAdmin(id).orElseThrow(() -> ProjectNotFoundException.withId(id));
    }
}
