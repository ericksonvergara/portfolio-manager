package com.portafolio_manager.backend.application.service.project;

import com.portafolio_manager.backend.application.port.in.project.SetProjectCoverUseCase;
import com.portafolio_manager.backend.application.port.out.image.ImageStoragePort;
import com.portafolio_manager.backend.application.port.out.project.ProjectRepositoryPort;
import com.portafolio_manager.backend.domain.project.AdminProject;
import com.portafolio_manager.backend.domain.project.ImageChange;
import com.portafolio_manager.backend.domain.project.ProjectNotFoundException;

public class SetProjectCoverService implements SetProjectCoverUseCase {

    private final ProjectRepositoryPort projectRepository;
    private final ImageStoragePort imageStorage;
    private final long maxImageBytes;

    public SetProjectCoverService(ProjectRepositoryPort projectRepository, ImageStoragePort imageStorage, long maxImageBytes) {
        this.projectRepository = projectRepository;
        this.imageStorage = imageStorage;
        this.maxImageBytes = maxImageBytes;
    }

    @Override
    public AdminProject execute(ImageChange change) {
        AdminProject current = projectRepository.findForAdmin(change.projectId())
                .orElseThrow(() -> ProjectNotFoundException.withId(change.projectId()));

        AdminProject updated = StoredImages.storeThen(change.image(), maxImageBytes, imageStorage, url ->
                projectRepository.setCover(change.projectId(), url)
                        .orElseThrow(() -> ProjectNotFoundException.withId(change.projectId())));

        if (current.coverImageUrl() != null) {
            imageStorage.delete(current.coverImageUrl());
        }
        return updated;
    }
}
