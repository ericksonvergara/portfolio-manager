package com.portafolio_manager.backend.infrastructure.web.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Token del enlace de recuperación y la contraseña nueva")
record PasswordResetConfirmRequest(
        @Schema(description = "Valor del parámetro token del enlace recibido por correo")
        @NotBlank @Size(max = 100) String token,

        @Schema(example = "Nueva#123", format = "password")
        @NotBlank @Size(max = 72) String newPassword
) {
}
