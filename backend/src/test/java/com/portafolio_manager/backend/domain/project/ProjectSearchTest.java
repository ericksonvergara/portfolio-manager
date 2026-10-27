package com.portafolio_manager.backend.domain.project;

import com.portafolio_manager.backend.domain.technology.Technology;
import com.portafolio_manager.backend.domain.technology.TechnologyCategory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProjectSearchTest {

    private static final ProjectSummary ERP = project("Gestión de Imágenes ERP", "Spring Boot", "React");

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void shouldMatchEverythingWhenBlank(String text) {
        assertThat(new ProjectSearch(text).matches(ERP)).isTrue();
        assertThat(ProjectSearch.all().matches(ERP)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"erp", "ERP", "imagenes", "IMÁGENES", "gestion", "  erp  "})
    void shouldFindByNameIgnoringCaseAndAccents(String text) {
        assertThat(new ProjectSearch(text).matches(ERP)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"react", "spring", "spring boot", "BOOT"})
    void shouldFindByTechnology(String text) {
        assertThat(new ProjectSearch(text).matches(ERP)).isTrue();
    }

    @Test
    void shouldRequireEveryWord() {
        assertThat(new ProjectSearch("erp react").matches(ERP)).isTrue();
        assertThat(new ProjectSearch("react   spring").matches(ERP)).isTrue();
        assertThat(new ProjectSearch("erp angular").matches(ERP)).isFalse();
    }

    @Test
    void shouldNotMatchOtherText() {
        assertThat(new ProjectSearch("angular").matches(ERP)).isFalse();
        // The description is not searched: only the name and the technologies (HU004).
        assertThat(new ProjectSearch("inventario").matches(ERP)).isFalse();
    }

    @Test
    void shouldLimitTheLength() {
        assertThat(new ProjectSearch("x".repeat(100)).text()).hasSize(100);
        assertThatThrownBy(() -> new ProjectSearch("x".repeat(101)))
                .isInstanceOf(InvalidProjectException.class)
                .hasMessage("search must be at most 100 characters");
    }

    private static ProjectSummary project(String name, String... technologies) {
        List<Technology> list = IntStream.range(0, technologies.length)
                .mapToObj(i -> new Technology((long) i, technologies[i], TechnologyCategory.BACKEND))
                .toList();
        return new ProjectSummary("erp", name, "Control de inventario.", "/images/erp.png", null, null,
                false, list);
    }
}
