package com.portafolio_manager.backend.infrastructure.web.admin.profile;

import com.portafolio_manager.backend.infrastructure.web.ApiErrorResponse;
import com.portafolio_manager.backend.infrastructure.web.profile.ProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.multipart.MultipartFile;

/**
 * OpenAPI documentation of the profile administration, kept apart from the controller logic.
 */
@Tag(name = "Administración - Perfil", description = "Perfil público del portafolio (requiere rol ADMIN)")
@ApiResponse(responseCode = "401", description = "Sin token o con un token inválido",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
@ApiResponse(responseCode = "403", description = "El usuario no es administrador",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
interface AdminProfileApi {

    @Operation(summary = "Consultar el perfil", description = "Los mismos datos que muestra la Landing Page.")
    @ApiResponse(responseCode = "200", description = "Perfil")
    @ApiResponse(responseCode = "404", description = "El perfil todavía no se ha creado",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    ProfileResponse getProfile();

    @Operation(
            summary = "Guardar el perfil",
            description = "Lo crea la primera vez y conserva la fotografía. El correo es el público de contacto."
    )
    @ApiResponse(responseCode = "200", description = "Perfil guardado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos: campos vacíos, correo o enlaces con formato inválido",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    ProfileResponse updateProfile(ProfileRequest request);

    @Operation(summary = "Subir o reemplazar la fotografía",
            description = "JPG o PNG hasta el tamaño máximo configurado; el archivo anterior se elimina.")
    @ApiResponse(responseCode = "200", description = "Perfil con la fotografía nueva")
    @ApiResponse(responseCode = "400", description = "El archivo no es una imagen JPG o PNG válida",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Guarda el perfil antes de subir la fotografía",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "413", description = "La imagen supera el tamaño máximo",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    ProfileResponse setPhoto(@Parameter(description = "Imagen JPG o PNG") MultipartFile file);

    @Operation(summary = "Quitar la fotografía", description = "La Landing Page muestra entonces las iniciales.")
    @ApiResponse(responseCode = "200", description = "Perfil sin fotografía")
    @ApiResponse(responseCode = "404", description = "El perfil todavía no se ha creado",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    ProfileResponse removePhoto();
}
