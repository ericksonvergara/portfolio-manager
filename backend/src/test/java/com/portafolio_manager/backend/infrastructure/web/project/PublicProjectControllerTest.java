package com.portafolio_manager.backend.infrastructure.web.project;

import com.portafolio_manager.backend.application.port.in.project.ListPublishedProjectsUseCase;
import com.portafolio_manager.backend.application.port.out.auth.RevokedAccessTokenRepositoryPort;
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
    private RevokedAccessTokenRepositoryPort revokedAccessTokenRepository;

    @Test
    void shouldReturnTheProjectCardsToAnonymousVisitors() throws Exception {
        when(listPublishedProjectsUseCase.execute(null)).thenReturn(List.of(new ProjectSummary(
                "sistema-erp",
                "Sistema ERP",
                "Gestión de inventario y facturación.",
                "/images/erp.png",
                null,
                "https://github.com/ericksonvergara/erp",
                true,
                List.of(
                        new Technology("Java", TechnologyCategory.BACKEND),
                        new Technology("React", TechnologyCategory.FRONTEND)
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
