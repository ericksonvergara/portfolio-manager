package com.portafolio_manager.backend.infrastructure.web.admin.technology;

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
 * OpenAPI documentation of the technology catalog administration, kept apart from the controller logic.
 */
@Tag(name = "Administración - Tecnologías", description = "Catálogo de tecnologías (requiere rol ADMIN)")
@ApiResponse(responseCode = "401", description = "Sin token o con un token inválido",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
@ApiResponse(responseCode = "403", description = "El usuario no es administrador",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
interface AdminTechnologyApi {

    @Operation(
            summary = "Listar el catálogo",
            description = "Por orden de aparición y nombre, con los proyectos que usan cada una."
    )
    @ApiResponse(responseCode = "200", description = "Tecnologías")
    List<AdminTechnologyResponse> listTechnologies();

    @Operation(summary = "Crear una tecnología", description = "El nombre no puede repetirse, sin importar mayúsculas.")
    @ApiResponse(responseCode = "201", description = "Tecnología creada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Ya existe una tecnología con ese nombre",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    ResponseEntity<AdminTechnologyResponse> createTechnology(TechnologyRequest request);

    @Operation(summary = "Editar una tecnología", description = "Los proyectos que la usan muestran el cambio.")
    @ApiResponse(responseCode = "200", description = "Tecnología actualizada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "No existe una tecnología con ese id",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Otra tecnología ya tiene ese nombre",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    AdminTechnologyResponse updateTechnology(
            @Parameter(description = "Id de la tecnología", example = "3") Long id,
            TechnologyRequest request
    );

    @Operation(summary = "Eliminar una tecnología",
            description = "Solo si ningún proyecto la usa: un proyecto debe tener al menos una tecnología (RN005).")
    @ApiResponse(responseCode = "204", description = "Tecnología eliminada")
    @ApiResponse(responseCode = "404", description = "No existe una tecnología con ese id",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "La usan uno o más proyectos",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    ResponseEntity<Void> deleteTechnology(@Parameter(description = "Id de la tecnología", example = "3") Long id);
}
