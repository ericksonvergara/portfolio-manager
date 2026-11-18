package com.portafolio_manager.backend.infrastructure.web.admin.dashboard;

import com.portafolio_manager.backend.infrastructure.web.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * OpenAPI documentation of the dashboard, kept apart from the controller logic.
 */
@Tag(name = "Administración - Dashboard", description = "Indicadores del portafolio (requiere rol ADMIN)")
@ApiResponse(responseCode = "401", description = "Sin token o con un token inválido",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
@ApiResponse(responseCode = "403", description = "El usuario no es administrador",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
interface AdminDashboardApi {

    @Operation(
            summary = "Consultar los indicadores",
            description = "Total de proyectos y de tecnologías, últimos proyectos registrado y publicado, último "
                    + "acceso (el anterior a la sesión actual) y visitas: totales, de hoy, del mes y proyecto más "
                    + "visto. El día y el mes siguen la zona horaria STATS_TIMEZONE."
    )
    @ApiResponse(responseCode = "200", description = "Indicadores")
    DashboardResponse getDashboard(@Parameter(hidden = true) Jwt jwt);
}
