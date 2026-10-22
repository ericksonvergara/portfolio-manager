package com.portafolio_manager.backend.application.service.project;

import com.portafolio_manager.backend.application.port.out.project.ProjectRepositoryPort;
import com.portafolio_manager.backend.domain.project.ProjectNotFoundException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeleteProjectServiceTest {

    private final ProjectRepositoryPort projectRepository = mock(ProjectRepositoryPort.class);
    private final DeleteProjectService service = new DeleteProjectService(projectRepository);

    @Test
    void shouldDeleteTheProject() {
        when(projectRepository.delete(7L)).thenReturn(true);

        assertThatCode(() -> service.execute(7L)).doesNotThrowAnyException();
    }

    @Test
    void shouldReportAnUnknownProject() {
        when(projectRepository.delete(8L)).thenReturn(false);

        assertThatThrownBy(() -> service.execute(8L))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessage("No project with id 8");
    }
}
