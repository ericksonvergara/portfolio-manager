package com.portafolio_manager.backend.application.service.project;

import com.portafolio_manager.backend.application.port.out.project.ProjectRepositoryPort;
import com.portafolio_manager.backend.domain.project.AdminProject;
import com.portafolio_manager.backend.domain.project.ProjectNotFoundException;
import com.portafolio_manager.backend.domain.project.ProjectNotPublishableException;
import com.portafolio_manager.backend.domain.project.ProjectPublication;
import com.portafolio_manager.backend.domain.project.ProjectStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SetProjectPublicationServiceTest {

    private final ProjectRepositoryPort projectRepository = mock(ProjectRepositoryPort.class);
    private final SetProjectPublicationService service = new SetProjectPublicationService(projectRepository);

    @Test
    void shouldPublishAProjectWithACoverImage() {
        AdminProject published = project("/uploads/erp.png", true);
        when(projectRepository.findForAdmin(7L)).thenReturn(Optional.of(project("/uploads/erp.png", false)));
        when(projectRepository.setActive(7L, true)).thenReturn(Optional.of(published));

        assertThat(service.execute(new ProjectPublication(7L, true))).isEqualTo(published);
    }

    @Test
    void shouldRejectPublishingAProjectWithoutCoverImage() {
        // RN004
        when(projectRepository.findForAdmin(7L)).thenReturn(Optional.of(project(null, false)));

        assertThatThrownBy(() -> service.execute(new ProjectPublication(7L, true)))
                .isInstanceOf(ProjectNotPublishableException.class)
                .hasMessageContaining("Sistema ERP");
        verify(projectRepository, never()).setActive(anyLong(), anyBoolean());
    }

    @Test
    void shouldHideAProjectEvenWithoutCoverImage() {
        AdminProject hidden = project(null, false);
        when(projectRepository.findForAdmin(7L)).thenReturn(Optional.of(project(null, true)));
        when(projectRepository.setActive(7L, false)).thenReturn(Optional.of(hidden));

        assertThat(service.execute(new ProjectPublication(7L, false))).isEqualTo(hidden);
    }

    @Test
    void shouldNotWriteWhenThePublicationDoesNotChange() {
        AdminProject current = project("/uploads/erp.png", true);
        when(projectRepository.findForAdmin(7L)).thenReturn(Optional.of(current));

        assertThat(service.execute(new ProjectPublication(7L, true))).isEqualTo(current);
        verify(projectRepository, never()).setActive(anyLong(), anyBoolean());
    }

    @Test
    void shouldReportAnUnknownProject() {
        when(projectRepository.findForAdmin(8L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new ProjectPublication(8L, true)))
                .isInstanceOf(ProjectNotFoundException.class);
    }

    private static AdminProject project(String cover, boolean active) {
        return new AdminProject(
                7L, "sistema-erp", "Sistema ERP", "Corta", "Completa", cover, null, null, null,
                ProjectStatus.COMPLETED, null, 1, false, active, List.of()
        );
    }
}
