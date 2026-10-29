package com.portafolio_manager.backend.infrastructure.web.admin.message;

import com.portafolio_manager.backend.domain.contact.ContactMessage;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Mensaje recibido desde el formulario de contacto")
record ContactMessageResponse(
        @Schema(example = "12")
        Long id,

        @Schema(example = "Ana Pérez")
        String name,

        @Schema(example = "ana@example.com")
        String email,

        @Schema(example = "Hola, me interesa tu trabajo. ¿Hablamos?")
        String message,

        @Schema(example = "2026-10-29T15:30:00Z")
        Instant receivedAt,

        @Schema(description = "Cuándo se marcó como leído; nulo si no se ha leído", nullable = true)
        Instant readAt,

        @Schema(example = "false")
        boolean read
) {

    static ContactMessageResponse from(ContactMessage message) {
        return new ContactMessageResponse(
                message.id(),
                message.name(),
                message.email(),
                message.message(),
                message.receivedAt(),
                message.readAt(),
                message.read()
        );
    }
}
