package com.portafolio_manager.backend.application.service.project;

import com.portafolio_manager.backend.application.port.out.project.ProjectRepositoryPort;
import com.portafolio_manager.backend.domain.project.AdminProject;
import com.portafolio_manager.backend.domain.project.ProjectNotFoundException;
import com.portafolio_manager.backend.domain.project.ProjectStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetAdminProjectServiceTest {

    private final ProjectRepositoryPort projectRepository = mock(ProjectRepositoryPort.class);
    private final GetAdminProjectService service = new GetAdminProjectService(projectRepository);

    @Test
    void shouldReturnTheProjectEvenWhenInactive() {
        AdminProject project = new AdminProject(
                7L, "sistema-erp", "Sistema ERP", "Corta", "Completa", null, null, null, null,
                ProjectStatus.COMPLETED, null, 1, false, false, List.of()
        );
        when(projectRepository.findForAdmin(7L)).thenReturn(Optional.of(project));

        assertThat(service.execute(7L)).isEqualTo(project);
    }

    @Test
    void shouldReportAnUnknownProject() {
        when(projectRepository.findForAdmin(8L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(8L))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessage("No project with id 8");
    }
}
