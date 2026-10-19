package com.portafolio_manager.backend.infrastructure.web.technology;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/**
 * OpenAPI documentation of the public technology catalog, kept apart from the controller logic.
 */
@Tag(name = "Público", description = "Contenido de la Landing Page, accesible sin autenticación")
interface PublicTechnologyApi {

    @Operation(
            summary = "Listar tecnologías",
            description = "Catálogo de tecnologías en orden de aparición. Devuelve una lista vacía si no hay ninguna."
    )
    @ApiResponse(responseCode = "200", description = "Catálogo de tecnologías")
    List<TechnologyResponse> listTechnologies();
}
