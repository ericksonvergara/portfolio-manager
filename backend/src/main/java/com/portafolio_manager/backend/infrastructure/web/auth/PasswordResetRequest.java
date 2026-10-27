package com.portafolio_manager.backend.infrastructure.web.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Correo de la cuenta que se quiere recuperar")
record PasswordResetRequest(
        @Schema(example = "admin@example.com")
        @NotBlank @Email @Size(max = 150) String email
) {
}
