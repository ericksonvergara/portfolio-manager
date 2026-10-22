package com.portafolio_manager.backend.application.service.project;

import com.portafolio_manager.backend.application.port.out.image.ImageStoragePort;
import com.portafolio_manager.backend.application.port.out.project.ProjectRepositoryPort;
import com.portafolio_manager.backend.domain.image.ImageFormat;
import com.portafolio_manager.backend.domain.image.ImageUpload;
import com.portafolio_manager.backend.domain.image.InvalidImageException;
import com.portafolio_manager.backend.domain.project.AdminProject;
import com.portafolio_manager.backend.domain.project.GalleryImage;
import com.portafolio_manager.backend.domain.project.GalleryImageRef;
import com.portafolio_manager.backend.domain.project.ImageChange;
import com.portafolio_manager.backend.domain.project.ProjectImageNotFoundException;
import com.portafolio_manager.backend.domain.project.ProjectNotPublishableException;
import com.portafolio_manager.backend.domain.project.ProjectStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Cover and gallery management (HU019): the services keep the stored files in step with the database.
 */
class ProjectImageServicesTest {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0};
    private static final ImageUpload VALID = new ImageUpload(PNG, "portada.png", "image/png");

    private final ProjectRepositoryPort projectRepository = mock(ProjectRepositoryPort.class);
    private final ImageStoragePort storage = mock(ImageStoragePort.class);

    @Test
    void shouldReplaceTheCoverAndDeleteThePreviousFile() {
        AdminProject updated = project("/uploads/new.png", false, List.of());
        when(projectRepository.findForAdmin(7L)).thenReturn(Optional.of(project("/uploads/old.png", false, List.of())));
        when(storage.store(PNG, ImageFormat.PNG)).thenReturn("/uploads/new.png");
        when(projectRepository.setCover(7L, "/uploads/new.png")).thenReturn(Optional.of(updated));

        AdminProject result = new SetProjectCoverService(projectRepository, storage, 1024)
                .execute(new ImageChange(7L, VALID, null));

        assertThat(result).isEqualTo(updated);
        verify(storage).delete("/uploads/old.png");
    }

    @Test
    void shouldDeleteTheNewFileWhenTheProjectCannotBeUpdated() {
        when(projectRepository.findForAdmin(7L)).thenReturn(Optional.of(project(null, false, List.of())));
        when(storage.store(PNG, ImageFormat.PNG)).thenReturn("/uploads/new.png");
        when(projectRepository.setCover(7L, "/uploads/new.png")).thenThrow(new IllegalStateException("database down"));

        assertThatThrownBy(() -> new SetProjectCoverService(projectRepository, storage, 1024)
                .execute(new ImageChange(7L, VALID, null))).isInstanceOf(IllegalStateException.class);
        verify(storage).delete("/uploads/new.png");
    }

    @Test
    void shouldStoreNothingWhenTheFileIsInvalid() {
        when(projectRepository.findForAdmin(7L)).thenReturn(Optional.of(project(null, false, List.of())));
        ImageUpload fake = new ImageUpload("not an image".getBytes(), "falsa.png", "image/png");

        assertThatThrownBy(() -> new SetProjectCoverService(projectRepository, storage, 1024)
                .execute(new ImageChange(7L, fake, null))).isInstanceOf(InvalidImageException.class);
        verify(storage, never()).store(any(), any());
    }

    @Test
    void shouldKeepTheCoverOfAPublishedProject() {
        // RN004
        when(projectRepository.findForAdmin(7L)).thenReturn(Optional.of(project("/uploads/cover.png", true, List.of())));

        assertThatThrownBy(() -> new RemoveProjectCoverService(projectRepository, storage).execute(7L))
                .isInstanceOf(ProjectNotPublishableException.class);
        verify(storage, never()).delete(any());
    }

    @Test
    void shouldRemoveTheCoverOfAnInactiveProject() {
        AdminProject updated = project(null, false, List.of());
        when(projectRepository.findForAdmin(7L)).thenReturn(Optional.of(project("/uploads/cover.png", false, List.of())));
        when(projectRepository.setCover(7L, null)).thenReturn(Optional.of(updated));

        assertThat(new RemoveProjectCoverService(projectRepository, storage).execute(7L)).isEqualTo(updated);
        verify(storage).delete("/uploads/cover.png");
    }

    @Test
    void shouldAddAGalleryImageWithItsAlternativeText() {
        AdminProject updated = project(null, false, List.of(new GalleryImage(12L, "/uploads/g.png", "Panel")));
        when(projectRepository.findForAdmin(7L)).thenReturn(Optional.of(project(null, false, List.of())));
        when(storage.store(PNG, ImageFormat.PNG)).thenReturn("/uploads/g.png");
        when(projectRepository.addGalleryImage(7L, "/uploads/g.png", "Panel")).thenReturn(Optional.of(updated));

        assertThat(new AddGalleryImageService(projectRepository, storage, 1024)
                .execute(new ImageChange(7L, VALID, "  Panel "))).isEqualTo(updated);
    }

    @Test
    void shouldRemoveAGalleryImageAndItsFile() {
        AdminProject updated = project(null, false, List.of());
        when(projectRepository.removeGalleryImage(7L, 12L)).thenReturn(Optional.of("/uploads/g.png"));
        when(projectRepository.findForAdmin(7L)).thenReturn(Optional.of(updated));

        assertThat(new RemoveGalleryImageService(projectRepository, storage).execute(new GalleryImageRef(7L, 12L)))
                .isEqualTo(updated);
        verify(storage).delete("/uploads/g.png");
    }

    @Test
    void shouldReportAnUnknownGalleryImage() {
        when(projectRepository.removeGalleryImage(anyLong(), anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new RemoveGalleryImageService(projectRepository, storage)
                .execute(new GalleryImageRef(7L, 99L)))
                .isInstanceOf(ProjectImageNotFoundException.class)
                .hasMessage("Project 7 has no image with id 99");
    }

    static AdminProject project(String cover, boolean active, List<GalleryImage> gallery) {
        return new AdminProject(
                7L, "sistema-erp", "Sistema ERP", "Corta", "Completa", cover, null, null, null,
                ProjectStatus.COMPLETED, null, 1, false, active, List.of(), gallery
        );
    }
}
