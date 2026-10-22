package com.portafolio_manager.backend.infrastructure.web.admin.project;

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

    @Operation(
            summary = "Consultar un proyecto",
            description = "Activo o inactivo, con los datos que edita el formulario."
    )
    @ApiResponse(responseCode = "200", description = "Proyecto encontrado")
    @ApiResponse(responseCode = "404", description = "No existe un proyecto con ese id",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    AdminProjectResponse getProject(@Parameter(description = "Id del proyecto", example = "7") Long id);

    @Operation(
            summary = "Editar un proyecto",
            description = "Reemplaza los datos del proyecto con las mismas validaciones que el registro. "
                    + "Si cambia el nombre, el slug de la URL pública se regenera. La publicación y las "
                    + "imágenes no cambian."
    )
    @ApiResponse(responseCode = "200", description = "Proyecto actualizado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos: campos obligatorios, URLs (RN002, RN003) "
            + "o tecnologías (RN005)",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "No existe un proyecto con ese id",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Otro proyecto ya tiene ese nombre (RN001)",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    AdminProjectResponse updateProject(
            @Parameter(description = "Id del proyecto", example = "7") Long id,
            ProjectRequest request
    );

    @Operation(
            summary = "Eliminar un proyecto",
            description = "Elimina el proyecto con su galería y sus tecnologías asociadas. El panel pide "
                    + "confirmación antes de llamarlo."
    )
    @ApiResponse(responseCode = "204", description = "Proyecto eliminado")
    @ApiResponse(responseCode = "404", description = "No existe un proyecto con ese id",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    ResponseEntity<Void> deleteProject(@Parameter(description = "Id del proyecto", example = "7") Long id);

    @Operation(
            summary = "Activar o desactivar un proyecto",
            description = "Un proyecto activo aparece en la Landing Page (RN008); desactivarlo lo oculta sin "
                    + "eliminarlo. Solo se puede activar si tiene imagen principal (RN004)."
    )
    @ApiResponse(responseCode = "200", description = "Publicación actualizada")
    @ApiResponse(responseCode = "400", description = "Falta el campo active",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "No existe un proyecto con ese id",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "El proyecto no tiene imagen principal (RN004)",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    AdminProjectResponse setPublication(
            @Parameter(description = "Id del proyecto", example = "7") Long id,
            PublicationRequest request
    );
}
