package com.portafolio_manager.backend.infrastructure.web.auth;

import com.portafolio_manager.backend.infrastructure.config.OpenApiConfig;
import com.portafolio_manager.backend.infrastructure.web.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

/**
 * OpenAPI documentation of the authentication endpoints, kept apart from the controller logic.
 */
@Tag(name = "Autenticación", description = "Inicio de sesión, renovación y cierre de sesión del administrador")
interface AuthApi {

    String REFRESH_COOKIE_DESCRIPTION =
            "Refresh token en cookie HttpOnly. El navegador la envía automáticamente; no es accesible desde JavaScript.";

    @Operation(
            summary = "Iniciar sesión",
            description = "Valida las credenciales del administrador. Devuelve un access token (JWT) "
                    + "y establece la cookie `refresh_token`."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Credenciales válidas",
            headers = @Header(name = "Set-Cookie", description = "Cookie `refresh_token` (HttpOnly, Secure, SameSite=Strict)")
    )
    @ApiResponse(responseCode = "400", description = "Cuerpo inválido (email mal formado o campos vacíos)",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Email o contraseña incorrectos",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    ResponseEntity<TokenResponse> login(LoginRequest request);

    @Operation(
            summary = "Renovar sesión",
            description = "Emite un nuevo access token y rota el refresh token. Reutilizar un refresh token "
                    + "ya rotado se considera robo y cierra todas las sesiones del usuario.",
            parameters = @Parameter(name = "refresh_token", in = ParameterIn.COOKIE,
                    description = REFRESH_COOKIE_DESCRIPTION, schema = @Schema(type = "string"))
    )
    @ApiResponse(
            responseCode = "200",
            description = "Sesión renovada",
            headers = @Header(name = "Set-Cookie", description = "Nueva cookie `refresh_token`")
    )
    @ApiResponse(responseCode = "401", description = "Refresh token ausente, inválido, expirado o revocado",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    ResponseEntity<TokenResponse> refresh(@Parameter(hidden = true) HttpServletRequest request);

    @Operation(
            summary = "Cerrar sesión",
            description = "Revoca el refresh token de la cookie y, si se envía, el access token actual, "
                    + "que deja de ser válido de inmediato. Siempre responde 204, aunque los tokens ya no sean válidos.",
            parameters = @Parameter(name = "refresh_token", in = ParameterIn.COOKIE,
                    description = REFRESH_COOKIE_DESCRIPTION, schema = @Schema(type = "string")),
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    )
    @ApiResponse(
            responseCode = "204",
            description = "Sesión cerrada",
            headers = @Header(name = "Set-Cookie", description = "Cookie `refresh_token` expirada")
    )
    ResponseEntity<Void> logout(@Parameter(hidden = true) HttpServletRequest request);
}
