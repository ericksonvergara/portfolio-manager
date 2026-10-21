package com.portafolio_manager.backend.application.service.project;

import com.portafolio_manager.backend.application.port.in.project.ListAdminProjectsUseCase;
import com.portafolio_manager.backend.application.port.out.project.ProjectRepositoryPort;
import com.portafolio_manager.backend.domain.project.AdminProject;

import java.util.List;

public class ListAdminProjectsService implements ListAdminProjectsUseCase {

    private final ProjectRepositoryPort projectRepository;

    public ListAdminProjectsService(ProjectRepositoryPort projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public List<AdminProject> execute(Void input) {
        return projectRepository.findAllForAdmin();
    }
}
