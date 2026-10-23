package com.portafolio_manager.backend.infrastructure.web.admin.account;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Contraseña actual y contraseña nueva")
record ChangePasswordRequest(
        @Schema(example = "Actual#123", format = "password")
        @NotBlank @Size(max = 72) String currentPassword,

        @Schema(example = "Nueva#456", format = "password")
        @NotBlank @Size(max = 72) String newPassword
) {
}
