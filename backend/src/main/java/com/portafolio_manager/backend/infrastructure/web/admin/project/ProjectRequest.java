package com.portafolio_manager.backend.infrastructure.web.admin.project;

import com.portafolio_manager.backend.domain.project.ProjectDraft;
import com.portafolio_manager.backend.domain.project.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

@Schema(description = "Datos de un proyecto. Las URLs y la fecha son opcionales; las imágenes se gestionan aparte")
record ProjectRequest(
        @Schema(example = "Sistema ERP")
        @NotBlank @Size(max = 100) String name,

        @Schema(example = "Gestión de inventario, compras y facturación.")
        @NotBlank @Size(max = 300) String shortDescription,

        @Schema(example = "Descripción completa del proyecto, sus objetivos y resultados.")
        @NotBlank @Size(max = 5000) String description,

        @Schema(example = "https://erp.example.com", nullable = true)
        @Size(max = 500) String projectUrl,

        @Schema(example = "https://github.com/usuario/erp", nullable = true)
        @Size(max = 500) String repositoryUrl,

        @Schema(example = "https://www.youtube.com/watch?v=abc123", nullable = true)
        @Size(max = 500) String videoUrl,

        @Schema(example = "COMPLETED")
        @NotNull ProjectStatus status,

        @Schema(example = "2026-10-01", nullable = true)
        @PastOrPresent LocalDate developedOn,

        @Schema(example = "1")
        @NotNull @PositiveOrZero Integer displayOrder,

        @Schema(example = "false")
        boolean featured,

        @Schema(description = "Ids del catálogo de tecnologías; al menos una (RN005)", example = "[1, 2]")
        @NotEmpty Set<@NotNull Long> technologyIds
) {

    ProjectDraft toDraft() {
        return new ProjectDraft(
                name,
                shortDescription,
                description,
                projectUrl,
                repositoryUrl,
                videoUrl,
                status,
                developedOn,
                displayOrder,
                featured,
                technologyIds
        );
    }
}
