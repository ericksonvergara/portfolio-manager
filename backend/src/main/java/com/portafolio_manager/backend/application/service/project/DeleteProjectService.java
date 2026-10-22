package com.portafolio_manager.backend.application.service.project;

import com.portafolio_manager.backend.application.port.in.project.DeleteProjectUseCase;
import com.portafolio_manager.backend.application.port.out.project.ProjectRepositoryPort;
import com.portafolio_manager.backend.domain.project.ProjectNotFoundException;

public class DeleteProjectService implements DeleteProjectUseCase {

    private final ProjectRepositoryPort projectRepository;

    public DeleteProjectService(ProjectRepositoryPort projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public Void execute(Long id) {
        if (!projectRepository.delete(id)) {
            throw ProjectNotFoundException.withId(id);
        }
        return null;
    }
}
