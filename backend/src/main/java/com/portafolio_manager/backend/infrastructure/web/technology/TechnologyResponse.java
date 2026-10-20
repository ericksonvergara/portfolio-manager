package com.portafolio_manager.backend.infrastructure.web.technology;

import com.portafolio_manager.backend.domain.technology.Technology;
import com.portafolio_manager.backend.domain.technology.TechnologyCategory;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tecnología del catálogo")
record TechnologyResponse(
        @Schema(example = "Spring Boot")
        String name,

        @Schema(example = "BACKEND")
        TechnologyCategory category
) {

    static TechnologyResponse from(Technology technology) {
        return new TechnologyResponse(technology.name(), technology.category());
    }
}
