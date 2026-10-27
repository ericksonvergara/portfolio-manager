package com.portafolio_manager.backend.infrastructure.web.admin.technology;

import com.portafolio_manager.backend.domain.technology.TechnologyCategory;
import com.portafolio_manager.backend.domain.technology.TechnologyDraft;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos de una tecnología del catálogo")
record TechnologyRequest(
        @Schema(example = "Spring Boot")
        @NotBlank @Size(max = 50) String name,

        @Schema(example = "BACKEND")
        @NotNull TechnologyCategory category,

        @Schema(example = "2", description = "Orden de aparición dentro del catálogo")
        @NotNull @PositiveOrZero Integer displayOrder
) {

    TechnologyDraft toDraft() {
        return new TechnologyDraft(name, category, displayOrder);
    }
}
