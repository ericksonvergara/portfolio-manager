package com.portafolio_manager.backend.infrastructure.web.visit;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "La página abierta; sin proyecto, es la Landing Page")
record VisitBody(
        @Schema(description = "Slug del proyecto cuando la página es su detalle", nullable = true, example = "sistema-erp")
        @Size(max = 120) String projectSlug
) {
}
