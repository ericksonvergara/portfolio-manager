package com.portafolio_manager.backend.application.service.project;

import com.portafolio_manager.backend.application.port.out.image.ImageStoragePort;
import com.portafolio_manager.backend.application.port.out.project.ProjectRepositoryPort;
import com.portafolio_manager.backend.domain.project.GalleryImage;
import com.portafolio_manager.backend.domain.project.ProjectNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeleteProjectServiceTest {

    private final ProjectRepositoryPort projectRepository = mock(ProjectRepositoryPort.class);
    private final ImageStoragePort storage = mock(ImageStoragePort.class);
    private final DeleteProjectService service = new DeleteProjectService(projectRepository, storage);

    @Test
    void shouldDeleteTheProjectAndItsImageFiles() {
        when(projectRepository.findForAdmin(7L)).thenReturn(Optional.of(ProjectImageServicesTest.project(
                "/uploads/cover.png", false, List.of(new GalleryImage(12L, "/uploads/g.png", null)))));
        when(projectRepository.delete(7L)).thenReturn(true);

        service.execute(7L);

        verify(storage).delete("/uploads/cover.png");
        verify(storage).delete("/uploads/g.png");
    }

    @Test
    void shouldReportAnUnknownProject() {
        when(projectRepository.findForAdmin(8L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(8L))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessage("No project with id 8");
        verify(projectRepository, never()).delete(8L);
        verify(storage, never()).delete(any());
    }
}
