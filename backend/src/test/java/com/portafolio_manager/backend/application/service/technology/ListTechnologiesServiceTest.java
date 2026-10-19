package com.portafolio_manager.backend.application.service.technology;

import com.portafolio_manager.backend.application.port.out.technology.TechnologyRepositoryPort;
import com.portafolio_manager.backend.domain.technology.Technology;
import com.portafolio_manager.backend.domain.technology.TechnologyCategory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ListTechnologiesServiceTest {

    @Test
    void shouldReturnTheCatalogInDisplayOrder() {
        List<Technology> catalog = List.of(
                new Technology("Java", TechnologyCategory.BACKEND),
                new Technology("React", TechnologyCategory.FRONTEND)
        );
        TechnologyRepositoryPort repository = () -> catalog;

        assertThat(new ListTechnologiesService(repository).execute(null)).containsExactlyElementsOf(catalog);
    }
}
