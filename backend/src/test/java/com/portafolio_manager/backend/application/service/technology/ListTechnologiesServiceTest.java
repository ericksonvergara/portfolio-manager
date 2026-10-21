package com.portafolio_manager.backend.application.service.technology;

import com.portafolio_manager.backend.application.port.out.technology.TechnologyRepositoryPort;
import com.portafolio_manager.backend.domain.technology.Technology;
import com.portafolio_manager.backend.domain.technology.TechnologyCategory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListTechnologiesServiceTest {

    @Test
    void shouldReturnTheCatalogInDisplayOrder() {
        List<Technology> catalog = List.of(
                new Technology(1L, "Java", TechnologyCategory.BACKEND),
                new Technology(2L, "React", TechnologyCategory.FRONTEND)
        );
        TechnologyRepositoryPort repository = mock(TechnologyRepositoryPort.class);
        when(repository.findAllInDisplayOrder()).thenReturn(catalog);

        assertThat(new ListTechnologiesService(repository).execute(null)).containsExactlyElementsOf(catalog);
    }
}
