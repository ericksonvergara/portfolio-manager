package com.portafolio_manager.backend.application.service.project;

import com.portafolio_manager.backend.application.port.in.project.RemoveGalleryImageUseCase;
import com.portafolio_manager.backend.application.port.out.image.ImageStoragePort;
import com.portafolio_manager.backend.application.port.out.project.ProjectRepositoryPort;
import com.portafolio_manager.backend.domain.project.AdminProject;
import com.portafolio_manager.backend.domain.project.GalleryImageRef;
import com.portafolio_manager.backend.domain.project.ProjectImageNotFoundException;
import com.portafolio_manager.backend.domain.project.ProjectNotFoundException;

public class RemoveGalleryImageService implements RemoveGalleryImageUseCase {

    private final ProjectRepositoryPort projectRepository;
    private final ImageStoragePort imageStorage;

    public RemoveGalleryImageService(ProjectRepositoryPort projectRepository, ImageStoragePort imageStorage) {
        this.projectRepository = projectRepository;
        this.imageStorage = imageStorage;
    }

    @Override
    public AdminProject execute(GalleryImageRef ref) {
        String url = projectRepository.removeGalleryImage(ref.projectId(), ref.imageId())
                .orElseThrow(() -> new ProjectImageNotFoundException(ref.projectId(), ref.imageId()));
        imageStorage.delete(url);
        return projectRepository.findForAdmin(ref.projectId())
                .orElseThrow(() -> ProjectNotFoundException.withId(ref.projectId()));
    }
}
