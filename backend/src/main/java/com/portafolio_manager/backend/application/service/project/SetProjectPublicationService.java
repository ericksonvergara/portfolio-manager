package com.portafolio_manager.backend.application.service.project;

import com.portafolio_manager.backend.application.port.in.project.SetProjectPublicationUseCase;
import com.portafolio_manager.backend.application.port.out.project.ProjectRepositoryPort;
import com.portafolio_manager.backend.domain.project.AdminProject;
import com.portafolio_manager.backend.domain.project.ProjectNotFoundException;
import com.portafolio_manager.backend.domain.project.ProjectNotPublishableException;
import com.portafolio_manager.backend.domain.project.ProjectPublication;

public class SetProjectPublicationService implements SetProjectPublicationUseCase {

    private final ProjectRepositoryPort projectRepository;

    public SetProjectPublicationService(ProjectRepositoryPort projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public AdminProject execute(ProjectPublication publication) {
        AdminProject project = projectRepository.findForAdmin(publication.id())
                .orElseThrow(() -> ProjectNotFoundException.withId(publication.id()));
        if (publication.active() && project.coverImageUrl() == null) {
            throw new ProjectNotPublishableException(project.name());
        }
        if (project.active() == publication.active()) {
            return project;
        }
        return projectRepository.setActive(publication.id(), publication.active())
                .orElseThrow(() -> ProjectNotFoundException.withId(publication.id()));
    }
}
