package com.portafolio_manager.backend.infrastructure.web.visit;

import com.portafolio_manager.backend.infrastructure.web.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

/**
 * OpenAPI documentation of the visit registration, kept apart from the controller logic.
 */
@Tag(name = "Público", description = "Contenido de la Landing Page, accesible sin autenticación")
interface VisitApi {

    @Operation(
            summary = "Registrar una visita",
            description = "La envía la web al abrir una página. Guarda la fecha, la IP enmascarada, el navegador, "
                    + "el sistema operativo, el dispositivo, el país y el proyecto cuando aplica. Si el mismo "
                    + "visitante reabre la misma página dentro de la ventana (VISIT_DEDUP_WINDOW), no suma otra "
                    + "visita, y la respuesta es la misma."
    )
    @ApiResponse(responseCode = "202", description = "Visita registrada")
    @ApiResponse(responseCode = "400", description = "El slug supera los 120 caracteres",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "No hay un proyecto publicado con ese slug",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    ResponseEntity<Void> registerVisit(
            VisitBody body,
            @Parameter(hidden = true) String userAgent,
            HttpServletRequest request
    );
}
