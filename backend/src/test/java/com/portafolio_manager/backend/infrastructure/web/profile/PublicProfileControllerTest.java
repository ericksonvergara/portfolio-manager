package com.portafolio_manager.backend.infrastructure.web.profile;

import com.portafolio_manager.backend.application.port.in.profile.GetPublicProfileUseCase;
import com.portafolio_manager.backend.application.port.out.auth.RevokedAccessTokenRepositoryPort;
import com.portafolio_manager.backend.domain.profile.Profile;
import com.portafolio_manager.backend.domain.profile.ProfileNotFoundException;
import com.portafolio_manager.backend.domain.profile.SocialLink;
import com.portafolio_manager.backend.domain.profile.SocialPlatform;
import com.portafolio_manager.backend.infrastructure.security.SecurityConfig;
import com.portafolio_manager.backend.infrastructure.security.jwt.TestJwt;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PublicProfileController.class, properties = TestJwt.SECRET_PROPERTY)
@Import(SecurityConfig.class)
class PublicProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetPublicProfileUseCase getPublicProfileUseCase;

    @MockitoBean
    private RevokedAccessTokenRepositoryPort revokedAccessTokenRepository;

    @Test
    void shouldReturnTheProfileToAnonymousVisitors() throws Exception {
        when(getPublicProfileUseCase.execute(null)).thenReturn(new Profile(
                "Erickson Vergara",
                "Desarrollador Full Stack",
                "Presentación",
                null,
                "contacto@example.com",
                "Colombia",
                List.of(
                        new SocialLink(SocialPlatform.GITHUB, "https://github.com/ericksonvergara"),
                        new SocialLink(SocialPlatform.LINKEDIN, "https://www.linkedin.com/in/example")
                )
        ));

        mockMvc.perform(get("/api/public/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Erickson Vergara"))
                .andExpect(jsonPath("$.headline").value("Desarrollador Full Stack"))
                .andExpect(jsonPath("$.photoUrl").doesNotExist())
                .andExpect(jsonPath("$.email").value("contacto@example.com"))
                .andExpect(jsonPath("$.socialLinks[0].platform").value("GITHUB"))
                .andExpect(jsonPath("$.socialLinks[1].url").value("https://www.linkedin.com/in/example"));
    }

    @Test
    void shouldAnswerNotFoundWhileTheProfileIsNotSetUp() throws Exception {
        when(getPublicProfileUseCase.execute(null)).thenThrow(new ProfileNotFoundException());

        mockMvc.perform(get("/api/public/profile"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/public/profile"));
    }
}
