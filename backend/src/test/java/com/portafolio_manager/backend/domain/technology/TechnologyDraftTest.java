package com.portafolio_manager.backend.domain.technology;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TechnologyDraftTest {

    @Test
    void shouldTrimTheName() {
        assertThat(new TechnologyDraft("  Spring Boot ", TechnologyCategory.BACKEND, 2).name()).isEqualTo("Spring Boot");
    }

    @Test
    void shouldRejectInvalidData() {
        assertThatThrownBy(() -> new TechnologyDraft(" ", TechnologyCategory.BACKEND, 1))
                .isInstanceOf(InvalidTechnologyException.class);
        assertThatThrownBy(() -> new TechnologyDraft("x".repeat(51), TechnologyCategory.BACKEND, 1))
                .isInstanceOf(InvalidTechnologyException.class);
        assertThatThrownBy(() -> new TechnologyDraft("Java", TechnologyCategory.BACKEND, -1))
                .isInstanceOf(InvalidTechnologyException.class);
    }
}
