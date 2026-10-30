package com.portafolio_manager.backend.infrastructure.web.contact;

import com.portafolio_manager.backend.domain.contact.ContactMessageDraft;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Mensaje del formulario de contacto")
record ContactRequest(
        @Schema(example = "Ana Pérez")
        @NotBlank @Size(max = 100) String name,

        @Schema(example = "ana@example.com")
        @NotBlank @Email @Size(max = 150) String email,

        @Schema(example = "Hola, me interesa tu trabajo. ¿Hablamos?")
        @NotBlank @Size(max = 2000) String message,

        @Schema(description = "Campo trampa contra el spam: el formulario lo oculta y debe llegar vacío",
                nullable = true, example = "")
        String website
) {

    /** Only a bot fills in the hidden field. */
    boolean isSpam() {
        return website != null && !website.isBlank();
    }

    ContactMessageDraft toDraft() {
        return new ContactMessageDraft(name, email, message);
    }
}
