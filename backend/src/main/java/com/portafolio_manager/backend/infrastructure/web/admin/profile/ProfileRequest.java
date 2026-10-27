package com.portafolio_manager.backend.infrastructure.web.admin.profile;

import com.portafolio_manager.backend.domain.profile.ProfileDraft;
import com.portafolio_manager.backend.domain.profile.SocialLink;
import com.portafolio_manager.backend.domain.profile.SocialPlatform;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Datos del perfil público; la fotografía se sube aparte")
record ProfileRequest(
        @Schema(example = "Erickson Vergara")
        @NotBlank @Size(max = 100) String fullName,

        @Schema(description = "Titular profesional", example = "Desarrollador Full Stack")
        @NotBlank @Size(max = 150) String headline,

        @Schema(description = "Presentación personal")
        @NotBlank @Size(max = 2000) String about,

        @Schema(description = "Correo público de contacto (no es el de inicio de sesión)", example = "contacto@example.com")
        @NotBlank @Email @Size(max = 150) String email,

        @Schema(nullable = true, example = "Bogotá, Colombia")
        @Size(max = 100) String location,

        @Schema(description = "Redes sociales en orden de aparición, una por plataforma")
        @NotNull @Size(max = 6) List<@Valid @NotNull SocialLinkRequest> socialLinks
) {

    ProfileDraft toDraft() {
        return new ProfileDraft(
                fullName,
                headline,
                about,
                email,
                location,
                socialLinks.stream().map(link -> new SocialLink(link.platform(), link.url().strip())).toList()
        );
    }

    record SocialLinkRequest(
            @Schema(example = "GITHUB")
            @NotNull SocialPlatform platform,

            @Schema(example = "https://github.com/ericksonvergara")
            @NotBlank @Size(max = 500) String url
    ) {
    }
}
