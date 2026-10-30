package com.portafolio_manager.backend.infrastructure.web.admin.message;

import com.portafolio_manager.backend.infrastructure.web.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * OpenAPI documentation of the contact inbox, kept apart from the controller logic.
 */
@Tag(name = "Administración - Mensajes", description = "Bandeja del formulario de contacto (requiere rol ADMIN)")
@ApiResponse(responseCode = "401", description = "Sin token o con un token inválido",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
@ApiResponse(responseCode = "403", description = "El usuario no es administrador",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
interface AdminMessageApi {

    @Operation(summary = "Listar los mensajes", description = "Del más reciente al más antiguo.")
    @ApiResponse(responseCode = "200", description = "Mensajes")
    List<ContactMessageResponse> listMessages();

    @Operation(summary = "Marcar un mensaje como leído", description = "Conserva la fecha de la primera lectura.")
    @ApiResponse(responseCode = "200", description = "Mensaje leído")
    @ApiResponse(responseCode = "404", description = "No existe un mensaje con ese id",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    ContactMessageResponse markRead(@Parameter(description = "Id del mensaje", example = "12") Long id);

    @Operation(summary = "Eliminar un mensaje")
    @ApiResponse(responseCode = "204", description = "Mensaje eliminado")
    @ApiResponse(responseCode = "404", description = "No existe un mensaje con ese id",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    ResponseEntity<Void> deleteMessage(@Parameter(description = "Id del mensaje", example = "12") Long id);
}
