package com.portafolio_manager.backend.infrastructure.web.admin.project;

import com.portafolio_manager.backend.application.port.in.project.AddGalleryImageUseCase;
import com.portafolio_manager.backend.application.port.in.project.RemoveGalleryImageUseCase;
import com.portafolio_manager.backend.application.port.in.project.RemoveProjectCoverUseCase;
import com.portafolio_manager.backend.application.port.in.project.SetProjectCoverUseCase;
import com.portafolio_manager.backend.application.port.out.auth.RevokedAccessTokenRepositoryPort;
import com.portafolio_manager.backend.domain.image.ImageTooLargeException;
import com.portafolio_manager.backend.domain.image.InvalidImageException;
import com.portafolio_manager.backend.domain.project.AdminProject;
import com.portafolio_manager.backend.domain.project.GalleryImage;
import com.portafolio_manager.backend.domain.project.ImageChange;
import com.portafolio_manager.backend.domain.project.ProjectNotPublishableException;
import com.portafolio_manager.backend.domain.project.ProjectStatus;
import com.portafolio_manager.backend.infrastructure.security.SecurityConfig;
import com.portafolio_manager.backend.infrastructure.security.jwt.TestJwt;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminProjectImageController.class, properties = TestJwt.SECRET_PROPERTY)
@Import(SecurityConfig.class)
class AdminProjectImageControllerTest {

    private static final MockMultipartFile FILE =
            new MockMultipartFile("file", "portada.png", "image/png", new byte[]{(byte) 0x89, 'P', 'N', 'G'});

    private static final AdminProject PROJECT = new AdminProject(
            7L, "sistema-erp", "Sistema ERP", "Corta", "Completa", "/uploads/cover.png", null, null, null,
            ProjectStatus.COMPLETED, null, 1, false, false, List.of(),
            List.of(new GalleryImage(12L, "/uploads/g.png", "Panel"))
    );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @MockitoBean
    private SetProjectCoverUseCase setProjectCoverUseCase;

    @MockitoBean
    private RemoveProjectCoverUseCase removeProjectCoverUseCase;

    @MockitoBean
    private AddGalleryImageUseCase addGalleryImageUseCase;

    @MockitoBean
    private RemoveGalleryImageUseCase removeGalleryImageUseCase;

    @MockitoBean
    private RevokedAccessTokenRepositoryPort revokedAccessTokenRepository;

    @Test
    void shouldUploadTheCover() throws Exception {
        when(setProjectCoverUseCase.execute(argThat((ImageChange c) -> c.projectId() == 7L
                && "portada.png".equals(c.image().originalFilename())))).thenReturn(PROJECT);

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/admin/projects/7/cover").file(FILE)
                        .header(HttpHeaders.AUTHORIZATION, admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coverImageUrl").value("/uploads/cover.png"))
                .andExpect(jsonPath("$.gallery[0].id").value(12))
                .andExpect(jsonPath("$.gallery[0].altText").value("Panel"));
    }

    @Test
    void shouldRejectInvalidOrTooLargeImages() throws Exception {
        when(setProjectCoverUseCase.execute(any())).thenThrow(new InvalidImageException("Only JPG, JPEG and PNG files are allowed"));
        mockMvc.perform(multipart(HttpMethod.PUT, "/api/admin/projects/7/cover").file(FILE)
                        .header(HttpHeaders.AUTHORIZATION, admin()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only JPG, JPEG and PNG files are allowed"));

        when(addGalleryImageUseCase.execute(any())).thenThrow(new ImageTooLargeException(5L * 1024 * 1024));
        mockMvc.perform(multipart("/api/admin/projects/7/images").file(FILE)
                        .header(HttpHeaders.AUTHORIZATION, admin()))
                .andExpect(status().isContentTooLarge())
                .andExpect(jsonPath("$.message").value("The image exceeds the maximum size of 5 MB"));
    }

    @Test
    void shouldRequireTheFilePart() throws Exception {
        mockMvc.perform(multipart(HttpMethod.PUT, "/api/admin/projects/7/cover").header(HttpHeaders.AUTHORIZATION, admin()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Missing file part 'file'"));
        verifyNoInteractions(setProjectCoverUseCase);
    }

    @Test
    void shouldAddAGalleryImageWithItsAlternativeText() throws Exception {
        when(addGalleryImageUseCase.execute(argThat((ImageChange c) -> "Panel".equals(c.altText()))))
                .thenReturn(PROJECT);

        mockMvc.perform(multipart("/api/admin/projects/7/images").file(FILE).param("altText", "Panel")
                        .header(HttpHeaders.AUTHORIZATION, admin()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gallery[0].url").value("/uploads/g.png"));
    }

    @Test
    void shouldAnswerConflictWhenRemovingTheCoverOfAPublishedProject() throws Exception {
        when(removeProjectCoverUseCase.execute(7L)).thenThrow(ProjectNotPublishableException.activeKeepsCover("Sistema ERP"));

        mockMvc.perform(delete("/api/admin/projects/7/cover").header(HttpHeaders.AUTHORIZATION, admin()))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldOnlyLetAnAdministratorManageImages() throws Exception {
        mockMvc.perform(multipart(HttpMethod.PUT, "/api/admin/projects/7/cover").file(FILE))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/admin/projects/7/images/12")).andExpect(status().isUnauthorized());
        verifyNoInteractions(setProjectCoverUseCase, removeGalleryImageUseCase);
    }

    private String admin() {
        return "Bearer " + TestJwt.token(jwtEncoder, TestJwt.ISSUER, Instant.now().plusSeconds(60), "ADMIN");
    }
}
