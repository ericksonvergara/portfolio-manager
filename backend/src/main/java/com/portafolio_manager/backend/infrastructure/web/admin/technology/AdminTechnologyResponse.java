package com.portafolio_manager.backend.infrastructure.web.admin.technology;

import com.portafolio_manager.backend.domain.technology.AdminTechnology;
import com.portafolio_manager.backend.domain.technology.TechnologyCategory;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tecnología del catálogo, con el número de proyectos que la usan")
record AdminTechnologyResponse(
        @Schema(example = "3") Long id,
        @Schema(example = "Spring Boot") String name,
        @Schema(example = "BACKEND") TechnologyCategory category,
        @Schema(example = "2") int displayOrder,
        @Schema(example = "4", description = "Mientras sea mayor que 0 no se puede eliminar") long projectCount
) {

    static AdminTechnologyResponse from(AdminTechnology technology) {
        return new AdminTechnologyResponse(
                technology.id(),
                technology.name(),
                technology.category(),
                technology.displayOrder(),
                technology.projectCount()
        );
    }
}
