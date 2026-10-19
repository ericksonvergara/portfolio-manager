package com.portafolio_manager.backend.application.service.project;

import com.portafolio_manager.backend.application.port.out.project.ProjectRepositoryPort;
import com.portafolio_manager.backend.domain.project.ProjectSummary;
import com.portafolio_manager.backend.domain.technology.Technology;
import com.portafolio_manager.backend.domain.technology.TechnologyCategory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListPublishedProjectsServiceTest {

    @Test
    void shouldReturnThePublishedProjects() {
        List<ProjectSummary> published = List.of(new ProjectSummary(
                "sistema-erp",
                "Sistema ERP",
                "Gestión de inventario y facturación.",
                "/images/erp.png",
                null,
                "https://github.com/ericksonvergara/erp",
                true,
                List.of(new Technology("Java", TechnologyCategory.BACKEND))
        ));
        ProjectRepositoryPort repository = mock(ProjectRepositoryPort.class);
        when(repository.findPublishedSummaries()).thenReturn(published);

        assertThat(new ListPublishedProjectsService(repository).execute(null)).containsExactlyElementsOf(published);
    }
}
