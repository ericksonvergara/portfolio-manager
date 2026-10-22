package com.portafolio_manager.backend.application.service.project;

import com.portafolio_manager.backend.application.port.in.project.AddGalleryImageUseCase;
import com.portafolio_manager.backend.application.port.out.image.ImageStoragePort;
import com.portafolio_manager.backend.application.port.out.project.ProjectRepositoryPort;
import com.portafolio_manager.backend.domain.project.AdminProject;
import com.portafolio_manager.backend.domain.project.ImageChange;
import com.portafolio_manager.backend.domain.project.ProjectNotFoundException;

public class AddGalleryImageService implements AddGalleryImageUseCase {

    private final ProjectRepositoryPort projectRepository;
    private final ImageStoragePort imageStorage;
    private final long maxImageBytes;

    public AddGalleryImageService(ProjectRepositoryPort projectRepository, ImageStoragePort imageStorage, long maxImageBytes) {
        this.projectRepository = projectRepository;
        this.imageStorage = imageStorage;
        this.maxImageBytes = maxImageBytes;
    }

    @Override
    public AdminProject execute(ImageChange change) {
        if (projectRepository.findForAdmin(change.projectId()).isEmpty()) {
            throw ProjectNotFoundException.withId(change.projectId());
        }
        return StoredImages.storeThen(change.image(), maxImageBytes, imageStorage, url ->
                projectRepository.addGalleryImage(change.projectId(), url, change.altText())
                        .orElseThrow(() -> ProjectNotFoundException.withId(change.projectId())));
    }
}
