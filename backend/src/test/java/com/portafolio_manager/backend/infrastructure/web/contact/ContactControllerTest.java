package com.portafolio_manager.backend.infrastructure.web.contact;

import com.portafolio_manager.backend.application.port.in.contact.SendContactMessageUseCase;
import com.portafolio_manager.backend.application.port.out.auth.RevokedAccessTokenRepositoryPort;
import com.portafolio_manager.backend.domain.contact.ContactMessage;
import com.portafolio_manager.backend.domain.contact.ContactSubmission;
import com.portafolio_manager.backend.domain.contact.TooManyContactMessagesException;
import com.portafolio_manager.backend.infrastructure.security.SecurityConfig;
import com.portafolio_manager.backend.infrastructure.security.jwt.TestJwt;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ContactController.class, properties = TestJwt.SECRET_PROPERTY)
@Import(SecurityConfig.class)
class ContactControllerTest {

    private static final String BODY = """
            { "name": "Ana Pérez", "email": "ana@example.com", "message": "Hola, ¿hablamos?", "website": "" }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SendContactMessageUseCase sendContactMessageUseCase;

    @MockitoBean
    private RevokedAccessTokenRepositoryPort revokedAccessTokenRepository;

    @Test
    void shouldAcceptAMessageWithoutAuthenticationAndCountItByTheRealIp() throws Exception {
        when(sendContactMessageUseCase.execute(any(ContactSubmission.class))).thenReturn(
                new ContactMessage(1L, "Ana Pérez", "ana@example.com", "Hola, ¿hablamos?", Instant.now(), null));

        mockMvc.perform(post("/api/public/contact")
                        .header("X-Real-IP", "203.0.113.7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isAccepted());

        verify(sendContactMessageUseCase).execute(argThat((ContactSubmission s) ->
                s.senderKey().equals("203.0.113.7") && s.draft().name().equals("Ana Pérez")));
    }

    @Test
    void shouldAnswerTheSameToABotButDiscardItsMessage() throws Exception {
        mockMvc.perform(post("/api/public/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY.replace("\"website\": \"\"", "\"website\": \"https://spam.example\"")))
                .andExpect(status().isAccepted());

        verifyNoInteractions(sendContactMessageUseCase);
    }

    @Test
    void shouldValidateTheDataBeforeSending() throws Exception {
        mockMvc.perform(post("/api/public/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY.replace("ana@example.com", "no-es-un-correo")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("email: must be a well-formed email address"));
        mockMvc.perform(post("/api/public/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY.replace("Hola, ¿hablamos?", " ")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(sendContactMessageUseCase);
    }

    @Test
    void shouldAnswerTooManyRequestsWhenTheSenderIsOverTheLimit() throws Exception {
        when(sendContactMessageUseCase.execute(any(ContactSubmission.class)))
                .thenThrow(new TooManyContactMessagesException());

        mockMvc.perform(post("/api/public/contact").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.message").value("Too many messages. Please try again later"));
    }
}
