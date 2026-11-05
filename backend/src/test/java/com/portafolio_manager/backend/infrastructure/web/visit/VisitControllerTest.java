package com.portafolio_manager.backend.infrastructure.web.visit;

import com.portafolio_manager.backend.application.port.in.visit.RegisterVisitUseCase;
import com.portafolio_manager.backend.application.port.out.auth.RevokedAccessTokenRepositoryPort;
import com.portafolio_manager.backend.domain.project.ProjectNotFoundException;
import com.portafolio_manager.backend.domain.visit.VisitRequest;
import com.portafolio_manager.backend.infrastructure.security.SecurityConfig;
import com.portafolio_manager.backend.infrastructure.security.jwt.TestJwt;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = VisitController.class, properties = TestJwt.SECRET_PROPERTY)
@Import(SecurityConfig.class)
class VisitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegisterVisitUseCase registerVisitUseCase;

    @MockitoBean
    private RevokedAccessTokenRepositoryPort revokedAccessTokenRepository;

    @Test
    void shouldRegisterAVisitToTheLandingPageWithoutBody() throws Exception {
        mockMvc.perform(post("/api/public/visits")
                        .header("X-Real-IP", "190.25.14.77")
                        .header(HttpHeaders.USER_AGENT, "Firefox/132.0"))
                .andExpect(status().isAccepted());

        verify(registerVisitUseCase).execute(new VisitRequest("190.25.14.77", "Firefox/132.0", null));
    }

    @Test
    void shouldPassTheProjectOfTheDetailPage() throws Exception {
        mockMvc.perform(post("/api/public/visits")
                        .header("X-Real-IP", "190.25.14.77")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"projectSlug\": \" sistema-erp \" }"))
                .andExpect(status().isAccepted());

        verify(registerVisitUseCase).execute(new VisitRequest("190.25.14.77", null, "sistema-erp"));
    }

    @Test
    void shouldAnswerNotFoundForAProjectThatIsNotPublished() throws Exception {
        when(registerVisitUseCase.execute(any(VisitRequest.class))).thenThrow(ProjectNotFoundException.withSlug("x"));
        mockMvc.perform(post("/api/public/visits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"projectSlug\": \"x\" }"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldValidateTheSlug() throws Exception {
        mockMvc.perform(post("/api/public/visits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"projectSlug\": \"" + "x".repeat(121) + "\" }"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(registerVisitUseCase);
    }
}
