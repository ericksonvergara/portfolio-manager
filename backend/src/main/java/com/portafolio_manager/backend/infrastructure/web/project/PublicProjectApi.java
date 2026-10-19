package com.portafolio_manager.backend.infrastructure.web.project;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/**
 * OpenAPI documentation of the public project endpoints, kept apart from the controller logic.
 */
@Tag(name = "Público", description = "Contenido de la Landing Page, accesible sin autenticación")
interface PublicProjectApi {

    @Operation(
            summary = "Listar proyectos publicados",
            description = "Solo proyectos activos: primero los destacados y después por orden de aparición. "
                    + "Devuelve una lista vacía si no hay ninguno."
    )
    @ApiResponse(responseCode = "200", description = "Proyectos publicados")
    List<ProjectCardResponse> listProjects();
}
