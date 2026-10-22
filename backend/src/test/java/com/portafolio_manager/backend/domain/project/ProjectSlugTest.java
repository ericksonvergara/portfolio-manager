package com.portafolio_manager.backend.domain.project;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProjectSlugTest {

    @Test
    void shouldBuildAReadableSlugWithoutAccentsOrSymbols() {
        assertThat(ProjectSlug.from("Gestión de Inventario 2.0")).isEqualTo("gestion-de-inventario-2-0");
        assertThat(ProjectSlug.from("  Diseño & Señales!  ")).isEqualTo("diseno-senales");
    }

    @Test
    void shouldRejectANameWithoutLettersOrDigits() {
        assertThatThrownBy(() -> ProjectSlug.from("¡¿!?"))
                .isInstanceOf(InvalidProjectException.class);
    }
}
