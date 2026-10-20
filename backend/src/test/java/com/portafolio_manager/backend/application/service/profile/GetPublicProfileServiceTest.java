package com.portafolio_manager.backend.application.service.profile;

import com.portafolio_manager.backend.application.port.out.profile.ProfileRepositoryPort;
import com.portafolio_manager.backend.domain.profile.Profile;
import com.portafolio_manager.backend.domain.profile.ProfileNotFoundException;
import com.portafolio_manager.backend.domain.profile.SocialLink;
import com.portafolio_manager.backend.domain.profile.SocialPlatform;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetPublicProfileServiceTest {

    private static final Profile PROFILE = new Profile(
            "Erickson Vergara",
            "Desarrollador Full Stack",
            "Presentación",
            "/images/profile.jpg",
            "contacto@example.com",
            "Colombia",
            List.of(new SocialLink(SocialPlatform.GITHUB, "https://github.com/ericksonvergara"))
    );

    @Test
    void shouldReturnTheProfile() {
        ProfileRepositoryPort repository = () -> Optional.of(PROFILE);

        assertThat(new GetPublicProfileService(repository).execute(null)).isEqualTo(PROFILE);
    }

    @Test
    void shouldFailWhileTheProfileIsNotSetUp() {
        ProfileRepositoryPort repository = Optional::empty;

        assertThatThrownBy(() -> new GetPublicProfileService(repository).execute(null))
                .isInstanceOf(ProfileNotFoundException.class);
    }
}
