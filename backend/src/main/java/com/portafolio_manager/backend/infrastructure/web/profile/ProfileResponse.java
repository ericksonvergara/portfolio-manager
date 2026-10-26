package com.portafolio_manager.backend.infrastructure.web.profile;

import com.portafolio_manager.backend.domain.profile.Profile;
import com.portafolio_manager.backend.domain.profile.SocialLink;
import com.portafolio_manager.backend.domain.profile.SocialPlatform;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Perfil público del dueño del portafolio")
public record ProfileResponse(
        @Schema(example = "Erickson Vergara")
        String fullName,

        @Schema(description = "Titular profesional", example = "Desarrollador Full Stack")
        String headline,

        @Schema(description = "Presentación personal")
        String about,

        @Schema(description = "URL de la fotografía; puede ser nula", example = "/images/profile.jpg", nullable = true)
        String photoUrl,

        @Schema(example = "contacto@example.com")
        String email,

        @Schema(nullable = true, example = "Bogotá, Colombia")
        String location,

        @Schema(description = "Redes sociales en orden de aparición")
        List<SocialLinkResponse> socialLinks
) {

    public static ProfileResponse from(Profile profile) {
        return new ProfileResponse(
                profile.fullName(),
                profile.headline(),
                profile.about(),
                profile.photoUrl(),
                profile.email(),
                profile.location(),
                profile.socialLinks().stream().map(SocialLinkResponse::from).toList()
        );
    }

    public record SocialLinkResponse(
            @Schema(example = "GITHUB")
            SocialPlatform platform,

            @Schema(example = "https://github.com/ericksonvergara")
            String url
    ) {

        static SocialLinkResponse from(SocialLink link) {
            return new SocialLinkResponse(link.platform(), link.url());
        }
    }
}
