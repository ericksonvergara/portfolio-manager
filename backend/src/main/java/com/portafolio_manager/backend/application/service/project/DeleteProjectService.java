package com.portafolio_manager.backend.application.service.project;

import com.portafolio_manager.backend.application.port.in.project.DeleteProjectUseCase;
import com.portafolio_manager.backend.application.port.out.image.ImageStoragePort;
import com.portafolio_manager.backend.application.port.out.project.ProjectRepositoryPort;
import com.portafolio_manager.backend.domain.project.AdminProject;
import com.portafolio_manager.backend.domain.project.GalleryImage;
import com.portafolio_manager.backend.domain.project.ProjectNotFoundException;

import java.util.Optional;

public class DeleteProjectService implements DeleteProjectUseCase {

    private final ProjectRepositoryPort projectRepository;
    private final ImageStoragePort imageStorage;

    public DeleteProjectService(ProjectRepositoryPort projectRepository, ImageStoragePort imageStorage) {
        this.projectRepository = projectRepository;
        this.imageStorage = imageStorage;
    }

    @Override
    public Void execute(Long id) {
        Optional<AdminProject> project = projectRepository.findForAdmin(id);
        if (project.isEmpty() || !projectRepository.delete(id)) {
            throw ProjectNotFoundException.withId(id);
        }
        // The files go only after the rows are gone, so a failed deletion never loses images.
        if (project.get().coverImageUrl() != null) {
            imageStorage.delete(project.get().coverImageUrl());
        }
        project.get().gallery().stream().map(GalleryImage::url).forEach(imageStorage::delete);
        return null;
    }
}
