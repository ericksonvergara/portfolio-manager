package com.portafolio_manager.backend.infrastructure.web.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Credenciales del administrador")
record LoginRequest(
        @Schema(example = "admin@example.com")
        @NotBlank @Email @Size(max = 150) String email,

        @Schema(example = "Secret#123", format = "password")
        @NotBlank @Size(max = 72) String password
) {
}
