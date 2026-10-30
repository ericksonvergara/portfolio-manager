package com.portafolio_manager.backend.application.service.project;

import com.portafolio_manager.backend.application.port.in.project.ListPublishedProjectsUseCase;
import com.portafolio_manager.backend.application.port.out.project.ProjectRepositoryPort;
import com.portafolio_manager.backend.domain.project.ProjectSearch;
import com.portafolio_manager.backend.domain.project.ProjectSummary;

import java.util.List;

public class ListPublishedProjectsService implements ListPublishedProjectsUseCase {

    private final ProjectRepositoryPort projectRepository;

    public ListPublishedProjectsService(ProjectRepositoryPort projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public List<ProjectSummary> execute(ProjectSearch search) {
        // The portfolio publishes a few dozen projects: they are filtered in memory, keeping the
        // accent-insensitive rule in the domain instead of depending on a database extension.
        return projectRepository.findPublishedSummaries().stream().filter(search::matches).toList();
    }
}
