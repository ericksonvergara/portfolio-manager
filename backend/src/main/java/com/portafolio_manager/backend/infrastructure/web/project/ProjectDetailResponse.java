package com.portafolio_manager.backend.infrastructure.web.project;

import com.portafolio_manager.backend.domain.project.ProjectDetail;
import com.portafolio_manager.backend.domain.project.ProjectImage;
import com.portafolio_manager.backend.domain.project.ProjectStatus;
import com.portafolio_manager.backend.domain.technology.Technology;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Detalle completo de un proyecto publicado")
record ProjectDetailResponse(
        @Schema(example = "sistema-erp")
        String slug,

        @Schema(example = "Sistema ERP")
        String name,

        @Schema(description = "Descripción completa")
        String description,

        @Schema(description = "Imagen principal", example = "/images/projects/sistema-erp.png")
        String coverImageUrl,

        @Schema(description = "URL del proyecto desplegado; puede ser nula", nullable = true, example = "https://erp.example.com")
        String projectUrl,

        @Schema(description = "URL del repositorio; puede ser nula", nullable = true, example = "https://github.com/ericksonvergara/erp")
        String repositoryUrl,

        @Schema(description = "Video demostrativo opcional", nullable = true, example = "https://www.youtube.com/watch?v=abc123")
        String videoUrl,

        @Schema(example = "COMPLETED")
        ProjectStatus status,

        @Schema(description = "Fecha de desarrollo", nullable = true, example = "2026-10-01")
        LocalDate developedOn,

        @Schema(description = "Galería de imágenes en orden de aparición")
        List<GalleryImageResponse> gallery,

        @Schema(description = "Nombres de las tecnologías utilizadas", example = "[\"Java\", \"Spring Boot\"]")
        List<String> technologies
) {

    static ProjectDetailResponse from(ProjectDetail project) {
        return new ProjectDetailResponse(
                project.slug(),
                project.name(),
                project.description(),
                project.coverImageUrl(),
                project.projectUrl(),
                project.repositoryUrl(),
                project.videoUrl(),
                project.status(),
                project.developedOn(),
                project.gallery().stream().map(GalleryImageResponse::from).toList(),
                project.technologies().stream().map(Technology::name).toList()
        );
    }

    record GalleryImageResponse(
            @Schema(example = "/images/projects/sistema-erp-1.png")
            String url,

            @Schema(description = "Texto alternativo; puede ser nulo", nullable = true, example = "Panel de facturación")
            String altText
    ) {

        static GalleryImageResponse from(ProjectImage image) {
            return new GalleryImageResponse(image.url(), image.altText());
        }
    }
}
