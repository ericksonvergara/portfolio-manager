package com.portafolio_manager.backend.application.service.project;

import com.portafolio_manager.backend.application.port.out.project.ProjectRepositoryPort;
import com.portafolio_manager.backend.application.port.out.technology.TechnologyRepositoryPort;
import com.portafolio_manager.backend.domain.project.AdminProject;
import com.portafolio_manager.backend.domain.project.DuplicateProjectNameException;
import com.portafolio_manager.backend.domain.project.InvalidProjectException;
import com.portafolio_manager.backend.domain.project.ProjectDraft;
import com.portafolio_manager.backend.domain.project.ProjectStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateProjectServiceTest {

    private final ProjectRepositoryPort projectRepository = mock(ProjectRepositoryPort.class);
    private final TechnologyRepositoryPort technologyRepository = mock(TechnologyRepositoryPort.class);
    private final CreateProjectService service = new CreateProjectService(projectRepository, technologyRepository);

    private final ProjectDraft draft = new ProjectDraft(
            "Sistema ERP", "Corta", "Completa", null, null, null,
            ProjectStatus.COMPLETED, null, 1, false, Set.of(1L, 2L)
    );

    @Test
    void shouldCreateTheProject() {
        AdminProject created = new AdminProject(
                7L, "sistema-erp", "Sistema ERP", "Corta", "Completa", null, null, null, null,
                ProjectStatus.COMPLETED, null, 1, false, false, List.of()
        );
        when(technologyRepository.findExistingIds(Set.of(1L, 2L))).thenReturn(Set.of(1L, 2L));
        when(projectRepository.create(draft)).thenReturn(created);

        assertThat(service.execute(draft)).isEqualTo(created);
    }

    @Test
    void shouldRejectADuplicatedName() {
        // RN001
        when(projectRepository.existsByNameOrSlug("Sistema ERP", "sistema-erp")).thenReturn(true);

        assertThatThrownBy(() -> service.execute(draft))
                .isInstanceOf(DuplicateProjectNameException.class)
                .hasMessageContaining("Sistema ERP");
        verify(projectRepository, never()).create(any());
    }

    @Test
    void shouldRejectTechnologiesThatAreNotInTheCatalog() {
        when(technologyRepository.findExistingIds(Set.of(1L, 2L))).thenReturn(Set.of(1L));

        assertThatThrownBy(() -> service.execute(draft))
                .isInstanceOf(InvalidProjectException.class)
                .hasMessageContaining("[2]");
        verify(projectRepository, never()).create(any());
    }
}
