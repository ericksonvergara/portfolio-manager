package com.portafolio_manager.backend.infrastructure.web.admin.project;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Publicación del proyecto: solo los activos aparecen en la Landing Page (RN008)")
record PublicationRequest(
        @Schema(example = "true")
        @NotNull Boolean active
) {
}
