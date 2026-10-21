package com.portafolio_manager.backend.infrastructure.web.admin.project;

import com.portafolio_manager.backend.infrastructure.web.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * OpenAPI documentation of the project administration endpoints, kept apart from the controller logic.
 */
@Tag(name = "Administración - Proyectos", description = "Gestión de los proyectos del portafolio (requiere rol ADMIN)")
@ApiResponse(responseCode = "401", description = "Sin token o con un token inválido",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
@ApiResponse(responseCode = "403", description = "El usuario no es administrador",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
interface AdminProjectApi {

    @Operation(
            summary = "Listar todos los proyectos",
            description = "Activos e inactivos, por orden de aparición y después por nombre."
    )
    @ApiResponse(responseCode = "200", description = "Proyectos")
    List<AdminProjectResponse> listProjects();

    @Operation(
            summary = "Registrar un proyecto",
            description = "Se crea inactivo: se podrá publicar cuando tenga imagen principal (RN004). "
                    + "El slug de la URL pública se genera a partir del nombre."
    )
    @ApiResponse(responseCode = "201", description = "Proyecto creado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos: campos obligatorios, URLs (RN002, RN003) "
            + "o tecnologías (RN005)",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Ya existe un proyecto con ese nombre (RN001)",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    ResponseEntity<AdminProjectResponse> createProject(ProjectRequest request);
}
