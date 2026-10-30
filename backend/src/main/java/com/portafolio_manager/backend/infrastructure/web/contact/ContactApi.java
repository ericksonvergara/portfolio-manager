package com.portafolio_manager.backend.infrastructure.web.contact;

import com.portafolio_manager.backend.infrastructure.web.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

/**
 * OpenAPI documentation of the contact form, kept apart from the controller logic.
 */
@Tag(name = "Público", description = "Contenido de la Landing Page, accesible sin autenticación")
interface ContactApi {

    @Operation(
            summary = "Enviar un mensaje de contacto",
            description = "Lo guarda para la bandeja del administrador y le avisa por correo. "
                    + "Cada IP puede enviar un número limitado de mensajes por hora (CONTACT_MAX_MESSAGES)."
    )
    @ApiResponse(responseCode = "202", description = "Mensaje recibido")
    @ApiResponse(responseCode = "400", description = "Datos inválidos: campos vacíos, correo con formato inválido",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "429", description = "Demasiados mensajes desde la misma IP; inténtalo más tarde",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    ResponseEntity<Void> sendMessage(ContactRequest request, HttpServletRequest servletRequest);
}
