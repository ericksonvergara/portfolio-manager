package com.portafolio_manager.backend.infrastructure.web.admin.account;

import com.portafolio_manager.backend.infrastructure.web.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * OpenAPI documentation of the account endpoints, kept apart from the controller logic.
 */
@Tag(name = "Administración - Cuenta", description = "Cuenta del administrador autenticado")
interface AdminAccountApi {

    @Operation(
            summary = "Cambiar la contraseña",
            description = "Pide la contraseña actual. La nueva debe cumplir la política (RF004), ser distinta de "
                    + "la actual y se guarda con BCrypt. Después se cierran todas las sesiones, también la de "
                    + "esta petición: hay que volver a iniciar sesión (HU011)."
    )
    @ApiResponse(responseCode = "204", description = "Contraseña cambiada y sesiones cerradas")
    @ApiResponse(responseCode = "400", description = "Contraseña actual incorrecta, o nueva que no cumple la "
            + "política o es igual a la actual",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Sin token o con un token inválido",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    ResponseEntity<Void> changePassword(@Parameter(hidden = true) Jwt jwt, ChangePasswordRequest request);
}
