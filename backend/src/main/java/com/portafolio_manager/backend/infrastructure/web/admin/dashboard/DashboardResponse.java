package com.portafolio_manager.backend.infrastructure.web.admin.dashboard;

import com.portafolio_manager.backend.domain.dashboard.DashboardSummary;
import com.portafolio_manager.backend.domain.dashboard.LatestProject;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Indicadores generales del portafolio")
record DashboardResponse(
        @Schema(example = "12", description = "Todos los proyectos, publicados o no") long totalProjects,
        @Schema(example = "24", description = "Tecnologías del catálogo") long totalTechnologies,
        @Schema(description = "Último proyecto registrado; nulo si no hay ninguno", nullable = true)
        LatestProjectResponse latestProject,
        @Schema(description = "Acceso anterior a la sesión actual; nulo en el primer acceso", nullable = true,
                example = "2026-11-04T21:15:00Z")
        Instant lastAccessAt
) {

    static DashboardResponse from(DashboardSummary summary) {
        LatestProject latest = summary.latestProject();
        return new DashboardResponse(
                summary.totalProjects(),
                summary.totalTechnologies(),
                latest == null ? null : new LatestProjectResponse(latest.name(), latest.registeredAt()),
                summary.lastAccessAt()
        );
    }

    @Schema(description = "Proyecto y fecha de registro")
    record LatestProjectResponse(
            @Schema(example = "Sistema ERP") String name,
            @Schema(example = "2026-11-02T15:30:00Z") Instant registeredAt
    ) {
    }
}
