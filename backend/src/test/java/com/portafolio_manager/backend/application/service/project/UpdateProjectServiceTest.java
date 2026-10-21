package com.portafolio_manager.backend.application.service.project;

import com.portafolio_manager.backend.application.port.out.project.ProjectRepositoryPort;
import com.portafolio_manager.backend.application.port.out.technology.TechnologyRepositoryPort;
import com.portafolio_manager.backend.domain.project.AdminProject;
import com.portafolio_manager.backend.domain.project.DuplicateProjectNameException;
import com.portafolio_manager.backend.domain.project.InvalidProjectException;
import com.portafolio_manager.backend.domain.project.ProjectDraft;
import com.portafolio_manager.backend.domain.project.ProjectNotFoundException;
import com.portafolio_manager.backend.domain.project.ProjectStatus;
import com.portafolio_manager.backend.domain.project.ProjectUpdate;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateProjectServiceTest {

    private final ProjectRepositoryPort projectRepository = mock(ProjectRepositoryPort.class);
    private final TechnologyRepositoryPort technologyRepository = mock(TechnologyRepositoryPort.class);
    private final UpdateProjectService service = new UpdateProjectService(projectRepository, technologyRepository);

    private final ProjectUpdate update = new ProjectUpdate(7L, new ProjectDraft(
            "Sistema ERP v2", "Corta", "Completa", null, null, null,
            ProjectStatus.COMPLETED, null, 1, false, Set.of(1L)
    ));

    @Test
    void shouldUpdateTheProject() {
        AdminProject updated = new AdminProject(
                7L, "sistema-erp-v2", "Sistema ERP v2", "Corta", "Completa", null, null, null, null,
                ProjectStatus.COMPLETED, null, 1, false, true, List.of()
        );
        when(technologyRepository.findExistingIds(Set.of(1L))).thenReturn(Set.of(1L));
        when(projectRepository.update(update)).thenReturn(Optional.of(updated));

        assertThat(service.execute(update)).isEqualTo(updated);
    }

    @Test
    void shouldRejectTheNameOfAnotherProject() {
        // RN001, ignoring the project being edited
        when(projectRepository.existsOtherByNameOrSlug(7L, "Sistema ERP v2", "sistema-erp-v2")).thenReturn(true);

        assertThatThrownBy(() -> service.execute(update)).isInstanceOf(DuplicateProjectNameException.class);
        verify(projectRepository, never()).update(any());
    }

    @Test
    void shouldRejectTechnologiesThatAreNotInTheCatalog() {
        when(technologyRepository.findExistingIds(Set.of(1L))).thenReturn(Set.of());

        assertThatThrownBy(() -> service.execute(update)).isInstanceOf(InvalidProjectException.class);
        verify(projectRepository, never()).update(any());
    }

    @Test
    void shouldReportAnUnknownProject() {
        when(technologyRepository.findExistingIds(Set.of(1L))).thenReturn(Set.of(1L));
        when(projectRepository.update(update)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(update))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessage("No project with id 7");
    }
}
