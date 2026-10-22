package com.portafolio_manager.backend.application.service.project;

import com.portafolio_manager.backend.application.port.in.project.RemoveProjectCoverUseCase;
import com.portafolio_manager.backend.application.port.out.image.ImageStoragePort;
import com.portafolio_manager.backend.application.port.out.project.ProjectRepositoryPort;
import com.portafolio_manager.backend.domain.project.AdminProject;
import com.portafolio_manager.backend.domain.project.ProjectNotFoundException;
import com.portafolio_manager.backend.domain.project.ProjectNotPublishableException;

public class RemoveProjectCoverService implements RemoveProjectCoverUseCase {

    private final ProjectRepositoryPort projectRepository;
    private final ImageStoragePort imageStorage;

    public RemoveProjectCoverService(ProjectRepositoryPort projectRepository, ImageStoragePort imageStorage) {
        this.projectRepository = projectRepository;
        this.imageStorage = imageStorage;
    }

    @Override
    public AdminProject execute(Long id) {
        AdminProject project = projectRepository.findForAdmin(id).orElseThrow(() -> ProjectNotFoundException.withId(id));
        if (project.coverImageUrl() == null) {
            return project;
        }
        if (project.active()) {
            throw ProjectNotPublishableException.activeKeepsCover(project.name()); // RN004
        }

        AdminProject updated = projectRepository.setCover(id, null).orElseThrow(() -> ProjectNotFoundException.withId(id));
        imageStorage.delete(project.coverImageUrl());
        return updated;
    }
}
