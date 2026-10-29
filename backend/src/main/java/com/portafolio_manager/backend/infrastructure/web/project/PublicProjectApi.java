package com.portafolio_manager.backend.infrastructure.web.project;

import com.portafolio_manager.backend.domain.project.ProjectCategory;
import com.portafolio_manager.backend.infrastructure.web.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/**
 * OpenAPI documentation of the public project endpoints, kept apart from the controller logic.
 */
@Tag(name = "Público", description = "Contenido de la Landing Page, accesible sin autenticación")
interface PublicProjectApi {

    @Operation(
            summary = "Listar, buscar y filtrar proyectos publicados",
            description = "Solo proyectos activos: primero los destacados y después por orden de aparición. "
                    + "Con `search`, solo los que coinciden por nombre o tecnología, sin distinguir mayúsculas "
                    + "ni tildes; con varias palabras deben coincidir todas. Con `category`, solo los de ese tipo, "
                    + "según sus tecnologías (FULL_STACK: usa tecnologías de backend y de frontend). Se pueden "
                    + "combinar. Devuelve una lista vacía si no hay ninguno."
    )
    @ApiResponse(responseCode = "200", description = "Proyectos publicados")
    @ApiResponse(responseCode = "400", description = "La búsqueda supera los 100 caracteres o la categoría no existe",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    List<ProjectCardResponse> listProjects(
            @Parameter(description = "Texto a buscar en el nombre y las tecnologías (opcional)", example = "react")
            String search,
            @Parameter(description = "Tipo de proyecto (opcional)", example = "FULL_STACK") ProjectCategory category
    );

    @Operation(
            summary = "Consultar el detalle de un proyecto",
            description = "Descripción completa, galería, tecnologías, fecha, estado, enlaces y video opcional."
    )
    @ApiResponse(responseCode = "200", description = "Proyecto encontrado")
    @ApiResponse(responseCode = "404", description = "No existe un proyecto publicado con ese slug (incluye los inactivos)",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    ProjectDetailResponse getProject(
            @Parameter(description = "Identificador legible del proyecto", example = "sistema-erp") String slug
    );
}
