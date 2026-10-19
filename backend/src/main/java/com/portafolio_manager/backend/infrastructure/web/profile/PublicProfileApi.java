package com.portafolio_manager.backend.infrastructure.web.profile;

import com.portafolio_manager.backend.infrastructure.web.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * OpenAPI documentation of the public profile endpoint, kept apart from the controller logic.
 */
@Tag(name = "Público", description = "Contenido de la Landing Page, accesible sin autenticación")
interface PublicProfileApi {

    @Operation(
            summary = "Consultar el perfil público",
            description = "Presentación, fotografía, redes sociales e información de contacto del dueño del portafolio."
    )
    @ApiResponse(responseCode = "200", description = "Perfil encontrado")
    @ApiResponse(responseCode = "404", description = "El perfil todavía no se ha configurado",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    ProfileResponse getProfile();
}
