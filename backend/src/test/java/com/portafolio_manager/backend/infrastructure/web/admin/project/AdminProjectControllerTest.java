package com.portafolio_manager.backend.infrastructure.web.admin.project;

import com.portafolio_manager.backend.application.port.in.project.CreateProjectUseCase;
import com.portafolio_manager.backend.application.port.in.project.GetAdminProjectUseCase;
import com.portafolio_manager.backend.application.port.in.project.ListAdminProjectsUseCase;
import com.portafolio_manager.backend.application.port.in.project.UpdateProjectUseCase;
import com.portafolio_manager.backend.application.port.out.auth.RevokedAccessTokenRepositoryPort;
import com.portafolio_manager.backend.domain.project.AdminProject;
import com.portafolio_manager.backend.domain.project.DuplicateProjectNameException;
import com.portafolio_manager.backend.domain.project.ProjectDraft;
import com.portafolio_manager.backend.domain.project.ProjectNotFoundException;
import com.portafolio_manager.backend.domain.project.ProjectStatus;
import com.portafolio_manager.backend.domain.project.ProjectUpdate;
import com.portafolio_manager.backend.domain.technology.Technology;
import com.portafolio_manager.backend.domain.technology.TechnologyCategory;
import com.portafolio_manager.backend.infrastructure.security.SecurityConfig;
import com.portafolio_manager.backend.infrastructure.security.jwt.TestJwt;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminProjectController.class, properties = TestJwt.SECRET_PROPERTY)
@Import(SecurityConfig.class)
class AdminProjectControllerTest {

    private static final String VALID_BODY = """
            {
              "name": "Sistema ERP",
              "shortDescription": "Gestión de inventario.",
              "description": "Descripción completa.",
              "projectUrl": "https://erp.example.com",
              "status": "COMPLETED",
              "developedOn": "2026-10-01",
              "displayOrder": 1,
              "featured": true,
              "technologyIds": [1]
            }
            """;

    private static final AdminProject CREATED = new AdminProject(
            7L, "sistema-erp", "Sistema ERP", "Gestión de inventario.", "Descripción completa.", null,
            "https://erp.example.com", null, null, ProjectStatus.COMPLETED, LocalDate.of(2026, 10, 1), 1,
            true, false, List.of(new Technology(1L, "Java", TechnologyCategory.BACKEND))
    );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @MockitoBean
    private ListAdminProjectsUseCase listAdminProjectsUseCase;

    @MockitoBean
    private CreateProjectUseCase createProjectUseCase;

    @MockitoBean
    private GetAdminProjectUseCase getAdminProjectUseCase;

    @MockitoBean
    private UpdateProjectUseCase updateProjectUseCase;

    @MockitoBean
    private RevokedAccessTokenRepositoryPort revokedAccessTokenRepository;

    @Test
    void shouldRejectAnonymousAndNonAdminRequests() throws Exception {
        mockMvc.perform(get("/api/admin/projects")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/projects").header(HttpHeaders.AUTHORIZATION, bearer("USER")))
                .andExpect(status().isForbidden());
        verifyNoInteractions(listAdminProjectsUseCase);
    }

    @Test
    void shouldListActiveAndInactiveProjects() throws Exception {
        when(listAdminProjectsUseCase.execute(null)).thenReturn(List.of(CREATED));

        mockMvc.perform(get("/api/admin/projects").header(HttpHeaders.AUTHORIZATION, bearer("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].active").value(false))
                .andExpect(jsonPath("$[0].technologies[0].id").value(1))
                .andExpect(jsonPath("$[0].technologies[0].name").value("Java"));
    }

    @Test
    void shouldCreateAProject() throws Exception {
        when(createProjectUseCase.execute(any(ProjectDraft.class))).thenReturn(CREATED);

        mockMvc.perform(post("/api/admin/projects")
                        .header(HttpHeaders.AUTHORIZATION, bearer("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/api/admin/projects/7"))
                .andExpect(jsonPath("$.slug").value("sistema-erp"))
                .andExpect(jsonPath("$.featured").value(true));
    }

    @Test
    void shouldRejectInvalidData() throws Exception {
        // RN005: no technologies
        mockMvc.perform(post("/api/admin/projects")
                        .header(HttpHeaders.AUTHORIZATION, bearer("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY.replace("[1]", "[]")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("technologyIds: must not be empty"));

        // RN002: invalid project URL, rejected by the domain
        mockMvc.perform(post("/api/admin/projects")
                        .header(HttpHeaders.AUTHORIZATION, bearer("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY.replace("https://erp.example.com", "erp.example.com")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("projectUrl must be a valid http or https URL"));

        verifyNoInteractions(createProjectUseCase);
    }

    @Test
    void shouldAnswerConflictForADuplicatedName() throws Exception {
        when(createProjectUseCase.execute(any(ProjectDraft.class)))
                .thenThrow(new DuplicateProjectNameException("Sistema ERP"));

        mockMvc.perform(post("/api/admin/projects")
                        .header(HttpHeaders.AUTHORIZATION, bearer("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.path").value("/api/admin/projects"));
    }

    @Test
    void shouldReturnOneProjectOrNotFound() throws Exception {
        when(getAdminProjectUseCase.execute(7L)).thenReturn(CREATED);
        when(getAdminProjectUseCase.execute(8L)).thenThrow(ProjectNotFoundException.withId(8L));

        mockMvc.perform(get("/api/admin/projects/7").header(HttpHeaders.AUTHORIZATION, bearer("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Descripción completa."));
        mockMvc.perform(get("/api/admin/projects/8").header(HttpHeaders.AUTHORIZATION, bearer("ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No project with id 8"));
    }

    @Test
    void shouldUpdateAProject() throws Exception {
        when(updateProjectUseCase.execute(argThat((ProjectUpdate u) -> u.id() == 7L && u.draft().featured())))
                .thenReturn(CREATED);

        mockMvc.perform(put("/api/admin/projects/7")
                        .header(HttpHeaders.AUTHORIZATION, bearer("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void shouldValidateTheUpdateLikeTheRegistration() throws Exception {
        mockMvc.perform(put("/api/admin/projects/7")
                        .header(HttpHeaders.AUTHORIZATION, bearer("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY.replace("\"Sistema ERP\"", "\"\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("name: must not be blank"));
        mockMvc.perform(put("/api/admin/projects/7").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(updateProjectUseCase);
    }

    private String bearer(String role) {
        return "Bearer " + TestJwt.token(jwtEncoder, TestJwt.ISSUER, Instant.now().plusSeconds(60), role);
    }
}
