package com.portafolio_manager.backend.application.service.technology;

import com.portafolio_manager.backend.application.port.out.technology.TechnologyRepositoryPort;
import com.portafolio_manager.backend.domain.technology.AdminTechnology;
import com.portafolio_manager.backend.domain.technology.DuplicateTechnologyNameException;
import com.portafolio_manager.backend.domain.technology.TechnologyCategory;
import com.portafolio_manager.backend.domain.technology.TechnologyDraft;
import com.portafolio_manager.backend.domain.technology.TechnologyInUseException;
import com.portafolio_manager.backend.domain.technology.TechnologyNotFoundException;
import com.portafolio_manager.backend.domain.technology.TechnologyUpdate;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Catalog management (HU021).
 */
class TechnologyAdminServicesTest {

    private static final TechnologyDraft DRAFT = new TechnologyDraft("Kotlin", TechnologyCategory.BACKEND, 3);
    private static final AdminTechnology KOTLIN = new AdminTechnology(9L, "Kotlin", TechnologyCategory.BACKEND, 3, 0);

    private final TechnologyRepositoryPort repository = mock(TechnologyRepositoryPort.class);

    @Test
    void shouldCreateATechnologyWithAnUnusedName() {
        when(repository.create(DRAFT)).thenReturn(KOTLIN);

        assertThat(new CreateTechnologyService(repository).execute(DRAFT)).isEqualTo(KOTLIN);
    }

    @Test
    void shouldRejectADuplicatedName() {
        when(repository.existsByNameIgnoringCase("Kotlin", null)).thenReturn(true);

        assertThatThrownBy(() -> new CreateTechnologyService(repository).execute(DRAFT))
                .isInstanceOf(DuplicateTechnologyNameException.class);
        verify(repository, never()).create(any());
    }

    @Test
    void shouldUpdateIgnoringItsOwnName() {
        TechnologyUpdate update = new TechnologyUpdate(9L, DRAFT);
        when(repository.existsByNameIgnoringCase("Kotlin", 9L)).thenReturn(false);
        when(repository.update(update)).thenReturn(Optional.of(KOTLIN));

        assertThat(new UpdateTechnologyService(repository).execute(update)).isEqualTo(KOTLIN);
    }

    @Test
    void shouldRejectTheNameOfAnotherTechnologyOrAnUnknownOne() {
        when(repository.existsByNameIgnoringCase("Kotlin", 9L)).thenReturn(true);
        assertThatThrownBy(() -> new UpdateTechnologyService(repository).execute(new TechnologyUpdate(9L, DRAFT)))
                .isInstanceOf(DuplicateTechnologyNameException.class);

        when(repository.update(new TechnologyUpdate(8L, DRAFT))).thenReturn(Optional.empty());
        assertThatThrownBy(() -> new UpdateTechnologyService(repository).execute(new TechnologyUpdate(8L, DRAFT)))
                .isInstanceOf(TechnologyNotFoundException.class);
    }

    @Test
    void shouldDeleteOnlyATechnologyThatNoProjectUses() {
        when(repository.findForAdmin(9L)).thenReturn(Optional.of(KOTLIN));
        when(repository.delete(9L)).thenReturn(true);
        new DeleteTechnologyService(repository).execute(9L);
        verify(repository).delete(9L);

        // RN005: the projects would be left without it
        when(repository.findForAdmin(1L)).thenReturn(Optional.of(
                new AdminTechnology(1L, "Java", TechnologyCategory.BACKEND, 1, 3)));
        assertThatThrownBy(() -> new DeleteTechnologyService(repository).execute(1L))
                .isInstanceOf(TechnologyInUseException.class)
                .hasMessageContaining("used by 3 project(s)");
        verify(repository, never()).delete(1L);
    }

    @Test
    void shouldReportAnUnknownTechnology() {
        when(repository.findForAdmin(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new DeleteTechnologyService(repository).execute(5L))
                .isInstanceOf(TechnologyNotFoundException.class)
                .hasMessage("No technology with id 5");
    }
}
