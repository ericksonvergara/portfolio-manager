package com.portafolio_manager.backend.application.service.project;

import com.portafolio_manager.backend.application.port.in.project.GetPublishedProjectUseCase;
import com.portafolio_manager.backend.application.port.out.project.ProjectRepositoryPort;
import com.portafolio_manager.backend.domain.project.ProjectDetail;
import com.portafolio_manager.backend.domain.project.ProjectNotFoundException;

public class GetPublishedProjectService implements GetPublishedProjectUseCase {

    private final ProjectRepositoryPort projectRepository;

    public GetPublishedProjectService(ProjectRepositoryPort projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public ProjectDetail execute(String slug) {
        return projectRepository.findPublishedBySlug(slug).orElseThrow(() -> ProjectNotFoundException.withSlug(slug));
    }
}
