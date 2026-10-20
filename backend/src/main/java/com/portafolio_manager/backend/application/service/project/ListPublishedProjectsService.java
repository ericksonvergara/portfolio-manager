package com.portafolio_manager.backend.application.service.project;

import com.portafolio_manager.backend.application.port.in.project.ListPublishedProjectsUseCase;
import com.portafolio_manager.backend.application.port.out.project.ProjectRepositoryPort;
import com.portafolio_manager.backend.domain.project.ProjectSummary;

import java.util.List;

public class ListPublishedProjectsService implements ListPublishedProjectsUseCase {

    private final ProjectRepositoryPort projectRepository;

    public ListPublishedProjectsService(ProjectRepositoryPort projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public List<ProjectSummary> execute(Void input) {
        return projectRepository.findPublishedSummaries();
    }
}
