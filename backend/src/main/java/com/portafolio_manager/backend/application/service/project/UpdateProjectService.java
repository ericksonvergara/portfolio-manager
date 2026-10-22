package com.portafolio_manager.backend.application.service.project;

import com.portafolio_manager.backend.application.port.in.project.UpdateProjectUseCase;
import com.portafolio_manager.backend.application.port.out.project.ProjectRepositoryPort;
import com.portafolio_manager.backend.application.port.out.technology.TechnologyRepositoryPort;
import com.portafolio_manager.backend.domain.project.AdminProject;
import com.portafolio_manager.backend.domain.project.DuplicateProjectNameException;
import com.portafolio_manager.backend.domain.project.ProjectDraft;
import com.portafolio_manager.backend.domain.project.ProjectNotFoundException;
import com.portafolio_manager.backend.domain.project.ProjectUpdate;

public class UpdateProjectService implements UpdateProjectUseCase {

    private final ProjectRepositoryPort projectRepository;
    private final TechnologyRepositoryPort technologyRepository;

    public UpdateProjectService(ProjectRepositoryPort projectRepository, TechnologyRepositoryPort technologyRepository) {
        this.projectRepository = projectRepository;
        this.technologyRepository = technologyRepository;
    }

    @Override
    public AdminProject execute(ProjectUpdate update) {
        ProjectDraft draft = update.draft();
        if (projectRepository.existsOtherByNameOrSlug(update.id(), draft.name(), draft.slug())) {
            throw new DuplicateProjectNameException(draft.name());
        }
        ProjectTechnologies.requireKnown(draft.technologyIds(), technologyRepository);

        return projectRepository.update(update).orElseThrow(() -> ProjectNotFoundException.withId(update.id()));
    }
}
