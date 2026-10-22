package com.portafolio_manager.backend.infrastructure.web.admin.project;

import com.portafolio_manager.backend.infrastructure.web.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/**
 * OpenAPI documentation of the project image endpoints (HU019), kept apart from the controller logic.
 */
@Tag(name = "Administración - Proyectos", description = "Gestión de los proyectos del portafolio (requiere rol ADMIN)")
@ApiResponse(responseCode = "401", description = "Sin token o con un token inválido",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
@ApiResponse(responseCode = "403", description = "El usuario no es administrador",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
@ApiResponse(responseCode = "404", description = "No existe el proyecto o la imagen",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
interface AdminProjectImageApi {

    String RULES = "Solo JPG, JPEG o PNG: se comprueban la extensión, el tipo MIME y el contenido real del "
            + "archivo, y el tamaño máximo configurado (RF011). Se guarda con un nombre único (RN007).";

    @Operation(summary = "Subir o reemplazar la imagen principal", description = RULES
            + " La imagen anterior se elimina.")
    @ApiResponse(responseCode = "200", description = "Imagen principal actualizada")
    @ApiResponse(responseCode = "400", description = "El archivo no es una imagen permitida",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "413", description = "La imagen supera el tamaño máximo",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    AdminProjectResponse setCover(
            @Parameter(description = "Id del proyecto", example = "7") Long id,
            @Parameter(description = "Imagen JPG, JPEG o PNG") MultipartFile file
    );

    @Operation(summary = "Quitar la imagen principal",
            description = "Solo si el proyecto está inactivo: un proyecto publicado necesita su imagen (RN004).")
    @ApiResponse(responseCode = "200", description = "Imagen principal eliminada")
    @ApiResponse(responseCode = "409", description = "El proyecto está publicado (RN004)",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    AdminProjectResponse removeCover(@Parameter(description = "Id del proyecto", example = "7") Long id);

    @Operation(summary = "Agregar una imagen a la galería", description = RULES
            + " Se añade al final de la galería.")
    @ApiResponse(responseCode = "201", description = "Imagen agregada")
    @ApiResponse(responseCode = "400", description = "El archivo no es una imagen permitida o el texto "
            + "alternativo supera los 200 caracteres",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "413", description = "La imagen supera el tamaño máximo",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    ResponseEntity<AdminProjectResponse> addGalleryImage(
            @Parameter(description = "Id del proyecto", example = "7") Long id,
            @Parameter(description = "Imagen JPG, JPEG o PNG") MultipartFile file,
            @Parameter(description = "Texto alternativo (accesibilidad), opcional", example = "Panel de facturación")
            String altText
    );

    @Operation(summary = "Quitar una imagen de la galería", description = "También elimina el archivo.")
    @ApiResponse(responseCode = "200", description = "Imagen eliminada")
    AdminProjectResponse removeGalleryImage(
            @Parameter(description = "Id del proyecto", example = "7") Long id,
            @Parameter(description = "Id de la imagen", example = "12") Long imageId
    );
}
