package com.portafolio_manager.backend.application.service.project;

import com.portafolio_manager.backend.application.port.in.project.CreateProjectUseCase;
import com.portafolio_manager.backend.application.port.out.project.ProjectRepositoryPort;
import com.portafolio_manager.backend.application.port.out.technology.TechnologyRepositoryPort;
import com.portafolio_manager.backend.domain.project.AdminProject;
import com.portafolio_manager.backend.domain.project.DuplicateProjectNameException;
import com.portafolio_manager.backend.domain.project.InvalidProjectException;
import com.portafolio_manager.backend.domain.project.ProjectDraft;

import java.util.Set;
import java.util.TreeSet;

public class CreateProjectService implements CreateProjectUseCase {

    private final ProjectRepositoryPort projectRepository;
    private final TechnologyRepositoryPort technologyRepository;

    public CreateProjectService(ProjectRepositoryPort projectRepository, TechnologyRepositoryPort technologyRepository) {
        this.projectRepository = projectRepository;
        this.technologyRepository = technologyRepository;
    }

    @Override
    public AdminProject execute(ProjectDraft draft) {
        if (projectRepository.existsByNameOrSlug(draft.name(), draft.slug())) {
            throw new DuplicateProjectNameException(draft.name());
        }

        Set<Long> unknown = new TreeSet<>(draft.technologyIds());
        unknown.removeAll(technologyRepository.findExistingIds(draft.technologyIds()));
        if (!unknown.isEmpty()) {
            throw new InvalidProjectException("Unknown technologies: " + unknown);
        }

        return projectRepository.create(draft);
    }
}
