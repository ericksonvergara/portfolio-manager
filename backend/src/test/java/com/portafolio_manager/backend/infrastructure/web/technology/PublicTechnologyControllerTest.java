package com.portafolio_manager.backend.infrastructure.web.technology;

import com.portafolio_manager.backend.application.port.in.technology.ListTechnologiesUseCase;
import com.portafolio_manager.backend.application.port.out.auth.RevokedAccessTokenRepositoryPort;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PublicTechnologyController.class, properties = TestJwt.SECRET_PROPERTY)
@Import(SecurityConfig.class)
class PublicTechnologyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListTechnologiesUseCase listTechnologiesUseCase;

    @MockitoBean
    private RevokedAccessTokenRepositoryPort revokedAccessTokenRepository;

    @Test
    void shouldReturnTheTechnologyCatalogToAnonymousVisitors() throws Exception {
        when(listTechnologiesUseCase.execute(null)).thenReturn(List.of(
                new Technology("Java", TechnologyCategory.BACKEND),
                new Technology("React", TechnologyCategory.FRONTEND)
        ));

        mockMvc.perform(get("/api/public/technologies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Java"))
                .andExpect(jsonPath("$[0].category").value("BACKEND"))
                .andExpect(jsonPath("$[1].name").value("React"));
    }
}
