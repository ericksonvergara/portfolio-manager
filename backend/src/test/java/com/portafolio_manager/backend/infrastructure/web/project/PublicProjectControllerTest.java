package com.portafolio_manager.backend.infrastructure.web.project;

import com.portafolio_manager.backend.application.port.in.project.GetPublishedProjectUseCase;
import com.portafolio_manager.backend.application.port.in.project.ListPublishedProjectsUseCase;
import com.portafolio_manager.backend.application.port.out.auth.RevokedAccessTokenRepositoryPort;
import com.portafolio_manager.backend.domain.project.ProjectDetail;
import com.portafolio_manager.backend.domain.project.ProjectImage;
import com.portafolio_manager.backend.domain.project.ProjectNotFoundException;
import com.portafolio_manager.backend.domain.project.ProjectSearch;
import com.portafolio_manager.backend.domain.project.ProjectStatus;
import com.portafolio_manager.backend.domain.project.ProjectSummary;
import com.portafolio_manager.backend.domain.technology.Technology;
import com.portafolio_manager.backend.domain.technology.TechnologyCategory;
import com.portafolio_manager.backend.infrastructure.security.SecurityConfig;
import com.portafolio_manager.backend.infrastructure.security.jwt.TestJwt;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PublicProjectController.class, properties = TestJwt.SECRET_PROPERTY)
@Import(SecurityConfig.class)
class PublicProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListPublishedProjectsUseCase listPublishedProjectsUseCase;

    @MockitoBean
    private GetPublishedProjectUseCase getPublishedProjectUseCase;

    @MockitoBean
    private RevokedAccessTokenRepositoryPort revokedAccessTokenRepository;

    @Test
    void shouldReturnTheProjectDetailToAnonymousVisitors() throws Exception {
        when(getPublishedProjectUseCase.execute("sistema-erp")).thenReturn(new ProjectDetail(
                "sistema-erp",
                "Sistema ERP",
                "Descripción completa del sistema.",
                "/images/erp.png",
                "https://erp.example.com",
                null,
                "https://www.youtube.com/watch?v=abc123",
                ProjectStatus.COMPLETED,
                LocalDate.of(2026, 10, 1),
                List.of(new ProjectImage("/images/erp-1.png", "Panel de facturación")),
                List.of(new Technology(1L, "Java", TechnologyCategory.BACKEND))
        ));

        mockMvc.perform(get("/api/public/projects/sistema-erp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("sistema-erp"))
                .andExpect(jsonPath("$.description").value("Descripción completa del sistema."))
                .andExpect(jsonPath("$.projectUrl").value("https://erp.example.com"))
                .andExpect(jsonPath("$.repositoryUrl").doesNotExist())
                .andExpect(jsonPath("$.videoUrl").value("https://www.youtube.com/watch?v=abc123"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.developedOn").value("2026-10-01"))
                .andExpect(jsonPath("$.gallery[0].url").value("/images/erp-1.png"))
                .andExpect(jsonPath("$.gallery[0].altText").value("Panel de facturación"))
                .andExpect(jsonPath("$.technologies").value(contains("Java")));
    }

    @Test
    void shouldAnswerNotFoundForAnUnknownOrInactiveProject() throws Exception {
        when(getPublishedProjectUseCase.execute("oculto")).thenThrow(ProjectNotFoundException.withSlug("oculto"));

        mockMvc.perform(get("/api/public/projects/oculto"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/public/projects/oculto"));
    }

    @Test
    void shouldPassTheSearchAndRejectOneThatIsTooLong() throws Exception {
        when(listPublishedProjectsUseCase.execute(new ProjectSearch("react"))).thenReturn(List.of());

        mockMvc.perform(get("/api/public/projects").param("search", " react "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
        mockMvc.perform(get("/api/public/projects").param("search", "x".repeat(101)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("search must be at most 100 characters"));
    }

    @Test
    void shouldReturnTheProjectCardsToAnonymousVisitors() throws Exception {
        when(listPublishedProjectsUseCase.execute(ProjectSearch.all())).thenReturn(List.of(new ProjectSummary(
                "sistema-erp",
                "Sistema ERP",
                "Gestión de inventario y facturación.",
                "/images/erp.png",
                null,
                "https://github.com/ericksonvergara/erp",
                true,
                List.of(
                        new Technology(2L, "Java", TechnologyCategory.BACKEND),
                        new Technology(3L, "React", TechnologyCategory.FRONTEND)
                )
        )));

        mockMvc.perform(get("/api/public/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slug").value("sistema-erp"))
                .andExpect(jsonPath("$[0].name").value("Sistema ERP"))
                .andExpect(jsonPath("$[0].shortDescription").value("Gestión de inventario y facturación."))
                .andExpect(jsonPath("$[0].coverImageUrl").value("/images/erp.png"))
                .andExpect(jsonPath("$[0].projectUrl").doesNotExist())
                .andExpect(jsonPath("$[0].repositoryUrl").value("https://github.com/ericksonvergara/erp"))
                .andExpect(jsonPath("$[0].featured").value(true))
                .andExpect(jsonPath("$[0].technologies").value(contains("Java", "React")));
    }
}
