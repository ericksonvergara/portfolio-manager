package com.portafolio_manager.backend.application.service.project;

import com.portafolio_manager.backend.application.port.out.project.ProjectRepositoryPort;
import com.portafolio_manager.backend.domain.project.ProjectDetail;
import com.portafolio_manager.backend.domain.project.ProjectNotFoundException;
import com.portafolio_manager.backend.domain.project.ProjectStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetPublishedProjectServiceTest {

    private final ProjectRepositoryPort repository = mock(ProjectRepositoryPort.class);
    private final GetPublishedProjectService service = new GetPublishedProjectService(repository);

    @Test
    void shouldReturnThePublishedProject() {
        ProjectDetail project = new ProjectDetail(
                "sistema-erp", "Sistema ERP", "Descripción completa", "/images/erp.png",
                null, null, null, ProjectStatus.COMPLETED, LocalDate.of(2026, 10, 1), List.of(), List.of()
        );
        when(repository.findPublishedBySlug("sistema-erp")).thenReturn(Optional.of(project));

        assertThat(service.execute("sistema-erp")).isEqualTo(project);
    }

    @Test
    void shouldFailWhenNoPublishedProjectHasTheSlug() {
        when(repository.findPublishedBySlug("desconocido")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("desconocido"))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessageContaining("desconocido");
    }
}
