package com.portafolio_manager.backend.infrastructure.web.admin.technology;

import com.portafolio_manager.backend.application.port.in.technology.CreateTechnologyUseCase;
import com.portafolio_manager.backend.application.port.in.technology.DeleteTechnologyUseCase;
import com.portafolio_manager.backend.application.port.in.technology.ListAdminTechnologiesUseCase;
import com.portafolio_manager.backend.application.port.in.technology.UpdateTechnologyUseCase;
import com.portafolio_manager.backend.application.port.out.auth.RevokedAccessTokenRepositoryPort;
import com.portafolio_manager.backend.domain.technology.AdminTechnology;
import com.portafolio_manager.backend.domain.technology.DuplicateTechnologyNameException;
import com.portafolio_manager.backend.domain.technology.TechnologyCategory;
import com.portafolio_manager.backend.domain.technology.TechnologyDraft;
import com.portafolio_manager.backend.domain.technology.TechnologyInUseException;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminTechnologyController.class, properties = TestJwt.SECRET_PROPERTY)
@Import(SecurityConfig.class)
class AdminTechnologyControllerTest {

    private static final AdminTechnology JAVA = new AdminTechnology(1L, "Java", TechnologyCategory.BACKEND, 1, 2);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @MockitoBean
    private ListAdminTechnologiesUseCase listAdminTechnologiesUseCase;

    @MockitoBean
    private CreateTechnologyUseCase createTechnologyUseCase;

    @MockitoBean
    private UpdateTechnologyUseCase updateTechnologyUseCase;

    @MockitoBean
    private DeleteTechnologyUseCase deleteTechnologyUseCase;

    @MockitoBean
    private RevokedAccessTokenRepositoryPort revokedAccessTokenRepository;

    @Test
    void shouldListTheCatalogWithItsUsage() throws Exception {
        when(listAdminTechnologiesUseCase.execute(null)).thenReturn(List.of(JAVA));

        mockMvc.perform(get("/api/admin/technologies").header(HttpHeaders.AUTHORIZATION, admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Java"))
                .andExpect(jsonPath("$[0].projectCount").value(2));
        mockMvc.perform(get("/api/admin/technologies")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldCreateATechnology() throws Exception {
        when(createTechnologyUseCase.execute(new TechnologyDraft("Kotlin", TechnologyCategory.BACKEND, 3)))
                .thenReturn(new AdminTechnology(9L, "Kotlin", TechnologyCategory.BACKEND, 3, 0));

        mockMvc.perform(post("/api/admin/technologies")
                        .header(HttpHeaders.AUTHORIZATION, admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Kotlin\", \"category\": \"BACKEND\", \"displayOrder\": 3}"))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/api/admin/technologies/9"));
    }

    @Test
    void shouldRejectInvalidDuplicatedOrUsedTechnologies() throws Exception {
        mockMvc.perform(post("/api/admin/technologies")
                        .header(HttpHeaders.AUTHORIZATION, admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Kotlin\", \"category\": \"COBOL\", \"displayOrder\": 3}"))
                .andExpect(status().isBadRequest());

        when(createTechnologyUseCase.execute(any())).thenThrow(new DuplicateTechnologyNameException("java"));
        mockMvc.perform(post("/api/admin/technologies")
                        .header(HttpHeaders.AUTHORIZATION, admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"java\", \"category\": \"BACKEND\", \"displayOrder\": 1}"))
                .andExpect(status().isConflict());

        doThrow(new TechnologyInUseException("Java", 2)).when(deleteTechnologyUseCase).execute(1L);
        mockMvc.perform(delete("/api/admin/technologies/1").header(HttpHeaders.AUTHORIZATION, admin()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Technology 'Java' is used by 2 project(s): remove it from them first"));
    }

    @Test
    void shouldOnlyLetAnAdministratorChangeTheCatalog() throws Exception {
        mockMvc.perform(delete("/api/admin/technologies/1")).andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/admin/technologies/1").header(HttpHeaders.AUTHORIZATION, bearer("USER")))
                .andExpect(status().isForbidden());
        verifyNoInteractions(deleteTechnologyUseCase);
    }

    private String admin() {
        return bearer("ADMIN");
    }

    private String bearer(String role) {
        return "Bearer " + TestJwt.token(jwtEncoder, TestJwt.ISSUER, Instant.now().plusSeconds(60), role);
    }
}
