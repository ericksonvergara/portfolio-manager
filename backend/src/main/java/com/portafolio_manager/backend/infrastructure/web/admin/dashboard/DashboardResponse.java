package com.portafolio_manager.backend.infrastructure.web.admin.dashboard;

import com.portafolio_manager.backend.domain.dashboard.DashboardSummary;
import com.portafolio_manager.backend.domain.dashboard.LatestProject;
import com.portafolio_manager.backend.domain.dashboard.LatestPublishedProject;
import com.portafolio_manager.backend.domain.dashboard.RankedProject;
import com.portafolio_manager.backend.domain.dashboard.VisitStatistics;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Indicadores del portafolio y estadísticas de visitas")
record DashboardResponse(
        @Schema(example = "12", description = "Todos los proyectos, publicados o no") long totalProjects,
        @Schema(example = "24", description = "Tecnologías del catálogo") long totalTechnologies,
        @Schema(description = "Último proyecto registrado; nulo si no hay ninguno", nullable = true)
        LatestProjectResponse latestProject,
        @Schema(description = "Último proyecto publicado entre los activos; nulo si no hay ninguno", nullable = true)
        LatestPublishedResponse latestPublishedProject,
        @Schema(description = "Acceso anterior a la sesión actual; nulo en el primer acceso", nullable = true,
                example = "2026-11-04T21:15:00Z")
        Instant lastAccessAt,
        VisitsResponse visits
) {

    static DashboardResponse from(DashboardSummary summary) {
        LatestProject latest = summary.latestProject();
        LatestPublishedProject published = summary.latestPublishedProject();
        VisitStatistics visits = summary.visits();
        return new DashboardResponse(
                summary.totalProjects(),
                summary.totalTechnologies(),
                latest == null ? null : new LatestProjectResponse(latest.name(), latest.registeredAt()),
                published == null ? null : new LatestPublishedResponse(published.name(), published.publishedAt()),
                summary.lastAccessAt(),
                new VisitsResponse(
                        visits.total(),
                        visits.today(),
                        visits.thisMonth(),
                        visits.mostVisitedProject()
                                .map(top -> new MostVisitedResponse(top.name(), top.visits()))
                                .orElse(null),
                        visits.ranking().stream().map(RankedProjectResponse::from).toList()
                )
        );
    }

    @Schema(description = "Proyecto y fecha de registro")
    record LatestProjectResponse(
            @Schema(example = "Sistema ERP") String name,
            @Schema(example = "2026-11-02T15:30:00Z") Instant registeredAt
    ) {
    }

    @Schema(description = "Proyecto y fecha de publicación")
    record LatestPublishedResponse(
            @Schema(example = "Sistema ERP") String name,
            @Schema(example = "2026-11-03T10:00:00Z") Instant publishedAt
    ) {
    }

    @Schema(description = "Visitas al sitio público, sin recargas; hoy y este mes según STATS_TIMEZONE")
    record VisitsResponse(
            @Schema(example = "12583") long total,
            @Schema(example = "54") long today,
            @Schema(example = "812") long thisMonth,
            @Schema(description = "Proyecto con más visitas a su detalle; nulo si no hay ninguna", nullable = true)
            MostVisitedResponse mostVisitedProject,
            @Schema(description = "Top de proyectos por visitas a su detalle, el más visitado primero (HU014)")
            List<RankedProjectResponse> ranking
    ) {
    }

    @Schema(description = "Proyecto del ranking")
    record RankedProjectResponse(
            @Schema(example = "7") Long id,
            @Schema(example = "Sistema ERP") String name,
            @Schema(example = "320") long visits,
            @Schema(example = "true", description = "Falso si se ocultó después: sus visitas siguen contando")
            boolean published
    ) {

        static RankedProjectResponse from(RankedProject project) {
            return new RankedProjectResponse(project.id(), project.name(), project.visits(), project.published());
        }
    }

    @Schema(description = "Proyecto y número de visitas")
    record MostVisitedResponse(
            @Schema(example = "Sistema ERP") String name,
            @Schema(example = "320") long visits
    ) {
    }
}
