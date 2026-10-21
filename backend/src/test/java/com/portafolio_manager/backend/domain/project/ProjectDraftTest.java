package com.portafolio_manager.backend.domain.project;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProjectDraftTest {

    @Test
    void shouldTrimTheTextAndTreatBlankUrlsAsNotSet() {
        ProjectDraft draft = draft("  Sistema ERP ", " https://erp.example.com ", "  ", Set.of(1L));

        assertThat(draft.name()).isEqualTo("Sistema ERP");
        assertThat(draft.projectUrl()).isEqualTo("https://erp.example.com");
        assertThat(draft.repositoryUrl()).isNull();
        assertThat(draft.slug()).isEqualTo("sistema-erp");
    }

    @Test
    void shouldRejectUrlsThatAreNotHttp() {
        // RN002 and RN003
        assertThatThrownBy(() -> draft("ERP", "ftp://erp.example.com", null, Set.of(1L)))
                .isInstanceOf(InvalidProjectException.class)
                .hasMessageContaining("projectUrl");
        assertThatThrownBy(() -> draft("ERP", null, "github.com/usuario/erp", Set.of(1L)))
                .isInstanceOf(InvalidProjectException.class)
                .hasMessageContaining("repositoryUrl");
        assertThatThrownBy(() -> draft("ERP", "https://", null, Set.of(1L)))
                .isInstanceOf(InvalidProjectException.class);
    }

    @Test
    void shouldRequireAtLeastOneTechnology() {
        // RN005
        assertThatThrownBy(() -> draft("ERP", null, null, Set.of()))
                .isInstanceOf(InvalidProjectException.class)
                .hasMessageContaining("technology");
    }

    @Test
    void shouldRequireTheTextFields() {
        assertThatThrownBy(() -> draft(" ", null, null, Set.of(1L)))
                .isInstanceOf(InvalidProjectException.class)
                .hasMessageContaining("name");
    }

    private static ProjectDraft draft(String name, String projectUrl, String repositoryUrl, Set<Long> technologies) {
        return new ProjectDraft(
                name,
                "Descripción corta",
                "Descripción completa",
                projectUrl,
                repositoryUrl,
                null,
                ProjectStatus.COMPLETED,
                LocalDate.of(2026, 10, 1),
                1,
                false,
                technologies
        );
    }
}
