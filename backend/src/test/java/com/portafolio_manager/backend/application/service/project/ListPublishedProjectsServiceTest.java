package com.portafolio_manager.backend.application.service.project;

import com.portafolio_manager.backend.application.port.out.project.ProjectRepositoryPort;
import com.portafolio_manager.backend.domain.project.ProjectSearch;
import com.portafolio_manager.backend.domain.project.ProjectSummary;
import com.portafolio_manager.backend.domain.technology.Technology;
import com.portafolio_manager.backend.domain.technology.TechnologyCategory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListPublishedProjectsServiceTest {

    private static final ProjectSummary ERP = summary("sistema-erp", "Sistema ERP", "Java");
    private static final ProjectSummary SHOP = summary("tienda", "Tienda en línea", "React");

    private final ProjectRepositoryPort repository = mock(ProjectRepositoryPort.class);

    @Test
    void shouldReturnThePublishedProjects() {
        when(repository.findPublishedSummaries()).thenReturn(List.of(ERP, SHOP));

        assertThat(new ListPublishedProjectsService(repository).execute(ProjectSearch.all())).containsExactly(ERP, SHOP);
    }

    @Test
    void shouldKeepOnlyTheProjectsThatMatchTheSearchInTheirOrder() {
        when(repository.findPublishedSummaries()).thenReturn(List.of(ERP, SHOP));

        assertThat(new ListPublishedProjectsService(repository).execute(new ProjectSearch("react"))).containsExactly(SHOP);
        assertThat(new ListPublishedProjectsService(repository).execute(new ProjectSearch("python"))).isEmpty();
    }

    private static ProjectSummary summary(String slug, String name, String technology) {
        return new ProjectSummary(slug, name, "Descripción corta.", "/images/" + slug + ".png", null,
                "https://github.com/ericksonvergara/" + slug, false,
                List.of(new Technology(1L, technology, TechnologyCategory.BACKEND)));
    }
}
