package com.portafolio_manager.backend.infrastructure.web.admin.profile;

import com.portafolio_manager.backend.application.port.in.profile.GetPublicProfileUseCase;
import com.portafolio_manager.backend.application.port.in.profile.RemoveProfilePhotoUseCase;
import com.portafolio_manager.backend.application.port.in.profile.SetProfilePhotoUseCase;
import com.portafolio_manager.backend.application.port.in.profile.UpdateProfileUseCase;
import com.portafolio_manager.backend.application.port.out.auth.RevokedAccessTokenRepositoryPort;
import com.portafolio_manager.backend.domain.image.ImageUpload;
import com.portafolio_manager.backend.domain.profile.InvalidProfileException;
import com.portafolio_manager.backend.domain.profile.Profile;
import com.portafolio_manager.backend.domain.profile.ProfileDraft;
import com.portafolio_manager.backend.domain.profile.ProfileNotFoundException;
import com.portafolio_manager.backend.domain.profile.SocialLink;
import com.portafolio_manager.backend.domain.profile.SocialPlatform;
import com.portafolio_manager.backend.infrastructure.security.SecurityConfig;
import com.portafolio_manager.backend.infrastructure.security.jwt.TestJwt;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminProfileController.class, properties = TestJwt.SECRET_PROPERTY)
@Import(SecurityConfig.class)
class AdminProfileControllerTest {

    private static final String VALID_BODY = """
            {
              "fullName": "Erickson Vergara",
              "headline": "Desarrollador Full Stack",
              "about": "Presentación.",
              "email": "contacto@example.com",
              "location": "Colombia",
              "socialLinks": [
                { "platform": "GITHUB", "url": "https://github.com/ericksonvergara" },
                { "platform": "WEBSITE", "url": " https://example.com " }
              ]
            }
            """;

    private static final Profile PROFILE = new Profile(
            "Erickson Vergara", "Desarrollador Full Stack", "Presentación.", "/uploads/foto.png",
            "contacto@example.com", "Colombia",
            List.of(new SocialLink(SocialPlatform.GITHUB, "https://github.com/ericksonvergara"))
    );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @MockitoBean
    private GetPublicProfileUseCase getProfileUseCase;

    @MockitoBean
    private UpdateProfileUseCase updateProfileUseCase;

    @MockitoBean
    private SetProfilePhotoUseCase setProfilePhotoUseCase;

    @MockitoBean
    private RemoveProfilePhotoUseCase removeProfilePhotoUseCase;

    @MockitoBean
    private RevokedAccessTokenRepositoryPort revokedAccessTokenRepository;

    @Test
    void shouldRejectAnonymousAndNonAdminRequests() throws Exception {
        mockMvc.perform(get("/api/admin/profile")).andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/admin/profile")
                        .header(HttpHeaders.AUTHORIZATION, bearer("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isForbidden());
        verifyNoInteractions(getProfileUseCase, updateProfileUseCase);
    }

    @Test
    void shouldReturnTheProfileOrNotFound() throws Exception {
        when(getProfileUseCase.execute(null)).thenReturn(PROFILE);
        mockMvc.perform(get("/api/admin/profile").header(HttpHeaders.AUTHORIZATION, bearer("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("contacto@example.com"))
                .andExpect(jsonPath("$.socialLinks[0].platform").value("GITHUB"));

        when(getProfileUseCase.execute(null)).thenThrow(new ProfileNotFoundException());
        mockMvc.perform(get("/api/admin/profile").header(HttpHeaders.AUTHORIZATION, bearer("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldSaveTheProfileWithItsLinksInOrder() throws Exception {
        when(updateProfileUseCase.execute(argThat((ProfileDraft draft) ->
                draft.fullName().equals("Erickson Vergara")
                        && draft.socialLinks().equals(List.of(
                                new SocialLink(SocialPlatform.GITHUB, "https://github.com/ericksonvergara"),
                                new SocialLink(SocialPlatform.WEBSITE, "https://example.com"))))))
                .thenReturn(PROFILE);

        mockMvc.perform(put("/api/admin/profile")
                        .header(HttpHeaders.AUTHORIZATION, bearer("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Erickson Vergara"));
    }

    @Test
    void shouldRejectInvalidData() throws Exception {
        mockMvc.perform(put("/api/admin/profile")
                        .header(HttpHeaders.AUTHORIZATION, bearer("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY.replace("contacto@example.com", "no-es-un-correo")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("email: must be a well-formed email address"));
        mockMvc.perform(put("/api/admin/profile")
                        .header(HttpHeaders.AUTHORIZATION, bearer("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY.replace("\"GITHUB\"", "\"MYSPACE\"")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(updateProfileUseCase);

        // The domain has the last word, for the rules the annotations cannot express.
        when(updateProfileUseCase.execute(any(ProfileDraft.class)))
                .thenThrow(new InvalidProfileException("email must be a valid email address"));
        mockMvc.perform(put("/api/admin/profile")
                        .header(HttpHeaders.AUTHORIZATION, bearer("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY.replace("contacto@example.com", "contacto@example")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("email must be a valid email address"));
    }

    @Test
    void shouldUploadAndRemoveThePhoto() throws Exception {
        when(setProfilePhotoUseCase.execute(argThat((ImageUpload upload) ->
                "foto.png".equals(upload.originalFilename())))).thenReturn(PROFILE);
        mockMvc.perform(multipart(HttpMethod.PUT, "/api/admin/profile/photo")
                        .file(new MockMultipartFile("file", "foto.png", "image/png", new byte[]{1}))
                        .header(HttpHeaders.AUTHORIZATION, bearer("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.photoUrl").value("/uploads/foto.png"));

        when(removeProfilePhotoUseCase.execute(null)).thenThrow(new ProfileNotFoundException());
        mockMvc.perform(delete("/api/admin/profile/photo").header(HttpHeaders.AUTHORIZATION, bearer("ADMIN")))
                .andExpect(status().isNotFound());
    }

    private String bearer(String role) {
        return "Bearer " + TestJwt.token(jwtEncoder, TestJwt.ISSUER, Instant.now().plusSeconds(60), role);
    }
}
