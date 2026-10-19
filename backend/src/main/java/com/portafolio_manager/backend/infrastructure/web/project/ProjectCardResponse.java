package com.portafolio_manager.backend.infrastructure.web.project;

import com.portafolio_manager.backend.domain.project.ProjectSummary;
import com.portafolio_manager.backend.domain.technology.Technology;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Tarjeta de un proyecto publicado")
record ProjectCardResponse(
        @Schema(description = "Identificador legible para la URL del detalle", example = "sistema-erp")
        String slug,

        @Schema(example = "Sistema ERP")
        String name,

        @Schema(example = "Gestión de inventario y facturación para pymes.")
        String shortDescription,

        @Schema(description = "Imagen principal", example = "/images/projects/sistema-erp.png")
        String coverImageUrl,

        @Schema(description = "URL del proyecto desplegado; puede ser nula", nullable = true, example = "https://erp.example.com")
        String projectUrl,

        @Schema(description = "URL del repositorio; puede ser nula", nullable = true, example = "https://github.com/ericksonvergara/erp")
        String repositoryUrl,

        @Schema(description = "Proyecto destacado")
        boolean featured,

        @Schema(description = "Nombres de las tecnologías utilizadas", example = "[\"Java\", \"Spring Boot\"]")
        List<String> technologies
) {

    static ProjectCardResponse from(ProjectSummary project) {
        return new ProjectCardResponse(
                project.slug(),
                project.name(),
                project.shortDescription(),
                project.coverImageUrl(),
                project.projectUrl(),
                project.repositoryUrl(),
                project.featured(),
                project.technologies().stream().map(Technology::name).toList()
        );
    }
}
