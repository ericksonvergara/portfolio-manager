package com.portafolio_manager.backend.infrastructure.web.admin.project;

import com.portafolio_manager.backend.domain.project.AdminProject;
import com.portafolio_manager.backend.domain.project.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Proyecto tal como lo ve el panel de administración, activo o no")
record AdminProjectResponse(
        @Schema(example = "7") Long id,
        @Schema(example = "sistema-erp") String slug,
        @Schema(example = "Sistema ERP") String name,
        @Schema(example = "Gestión de inventario, compras y facturación.") String shortDescription,
        @Schema(example = "Descripción completa del proyecto.") String description,
        @Schema(example = "/uploads/3f2a.png", nullable = true) String coverImageUrl,
        @Schema(example = "https://erp.example.com", nullable = true) String projectUrl,
        @Schema(example = "https://github.com/usuario/erp", nullable = true) String repositoryUrl,
        @Schema(nullable = true) String videoUrl,
        @Schema(example = "COMPLETED") ProjectStatus status,
        @Schema(example = "2026-10-01", nullable = true) LocalDate developedOn,
        @Schema(example = "1") int displayOrder,
        @Schema(example = "false") boolean featured,
        @Schema(example = "false", description = "Solo los activos aparecen en la Landing Page (RN008)") boolean active,
        List<TechnologyRef> technologies,
        @Schema(description = "Galería, en orden de aparición") List<ImageRef> gallery
) {

    @Schema(description = "Tecnología asociada al proyecto")
    record TechnologyRef(@Schema(example = "1") Long id, @Schema(example = "Java") String name) {
    }

    @Schema(description = "Imagen de la galería")
    record ImageRef(
            @Schema(example = "12") Long id,
            @Schema(example = "/uploads/3f2a.png") String url,
            @Schema(example = "Panel de facturación", nullable = true) String altText
    ) {
    }

    static AdminProjectResponse from(AdminProject project) {
        return new AdminProjectResponse(
                project.id(),
                project.slug(),
                project.name(),
                project.shortDescription(),
                project.description(),
                project.coverImageUrl(),
                project.projectUrl(),
                project.repositoryUrl(),
                project.videoUrl(),
                project.status(),
                project.developedOn(),
                project.displayOrder(),
                project.featured(),
                project.active(),
                project.technologies().stream()
                        .map(technology -> new TechnologyRef(technology.id(), technology.name()))
                        .toList(),
                project.gallery().stream()
                        .map(image -> new ImageRef(image.id(), image.url(), image.altText()))
                        .toList()
        );
    }
}
