package com.portafolio_manager.backend.domain.contact;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContactMessageDraftTest {

    @Test
    void shouldTrimTheFields() {
        ContactMessageDraft draft = new ContactMessageDraft("  Ana Pérez ", " ana@example.com ", "  Hola.  ");

        assertThat(draft.name()).isEqualTo("Ana Pérez");
        assertThat(draft.email()).isEqualTo("ana@example.com");
        assertThat(draft.message()).isEqualTo("Hola.");
    }

    @Test
    void shouldRequireEveryField() {
        assertThatThrownBy(() -> new ContactMessageDraft(" ", "ana@example.com", "Hola"))
                .isInstanceOf(InvalidContactMessageException.class)
                .hasMessage("name must not be blank");
        assertThatThrownBy(() -> new ContactMessageDraft("Ana", null, "Hola")).hasMessage("email must not be blank");
        assertThatThrownBy(() -> new ContactMessageDraft("Ana", "ana@example.com", ""))
                .hasMessage("message must not be blank");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ana", "ana@", "ana@example", "ana pérez@example.com"})
    void shouldRejectAnInvalidEmail(String email) {
        assertThatThrownBy(() -> new ContactMessageDraft("Ana", email, "Hola"))
                .isInstanceOf(InvalidContactMessageException.class)
                .hasMessage("email must be a valid email address");
    }

    @Test
    void shouldLimitTheLength() {
        assertThatThrownBy(() -> new ContactMessageDraft("x".repeat(101), "ana@example.com", "Hola"))
                .hasMessage("name must be at most 100 characters");
        assertThatThrownBy(() -> new ContactMessageDraft("Ana", "ana@example.com", "x".repeat(2001)))
                .hasMessage("message must be at most 2000 characters");
        assertThat(new ContactMessageDraft("Ana", "ana@example.com", "x".repeat(2000)).message()).hasSize(2000);
    }
}
