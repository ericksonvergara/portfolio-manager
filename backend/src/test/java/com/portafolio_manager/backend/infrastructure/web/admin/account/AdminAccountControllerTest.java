package com.portafolio_manager.backend.infrastructure.web.admin.account;

import com.portafolio_manager.backend.application.port.in.auth.ChangePasswordCommand;
import com.portafolio_manager.backend.application.port.in.auth.ChangePasswordUseCase;
import com.portafolio_manager.backend.application.port.out.auth.RevokedAccessTokenRepositoryPort;
import com.portafolio_manager.backend.domain.user.IncorrectPasswordException;
import com.portafolio_manager.backend.infrastructure.security.SecurityConfig;
import com.portafolio_manager.backend.infrastructure.security.jwt.TestJwt;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminAccountController.class, properties = TestJwt.SECRET_PROPERTY)
@Import(SecurityConfig.class)
class AdminAccountControllerTest {

    private static final String BODY = "{\"currentPassword\": \"Actual#123\", \"newPassword\": \"Nueva#456\"}";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @MockitoBean
    private ChangePasswordUseCase changePasswordUseCase;

    @MockitoBean
    private RevokedAccessTokenRepositoryPort revokedAccessTokenRepository;

    @Test
    void shouldChangeThePasswordOfTheSignedInUser() throws Exception {
        Instant expiresAt = Instant.now().plusSeconds(300).truncatedTo(ChronoUnit.SECONDS);
        String token = TestJwt.token(jwtEncoder, "jti-42", TestJwt.ISSUER, expiresAt, "ADMIN");

        mockMvc.perform(put("/api/admin/account/password")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isNoContent());

        ArgumentCaptor<ChangePasswordCommand> command = ArgumentCaptor.forClass(ChangePasswordCommand.class);
        verify(changePasswordUseCase).execute(command.capture());
        // The user comes from the token (subject "1"), never from the request body.
        assertThat(command.getValue()).isEqualTo(
                new ChangePasswordCommand(1L, "Actual#123", "Nueva#456", "jti-42", expiresAt));
    }

    @Test
    void shouldAnswerBadRequestAndNotUnauthorizedForAWrongCurrentPassword() throws Exception {
        doThrow(new IncorrectPasswordException()).when(changePasswordUseCase).execute(any());

        mockMvc.perform(put("/api/admin/account/password")
                        .header(HttpHeaders.AUTHORIZATION, admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("The current password is incorrect"));
    }

    @Test
    void shouldRequireAnAdministratorAndBothPasswords() throws Exception {
        mockMvc.perform(put("/api/admin/account/password").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/admin/account/password")
                        .header(HttpHeaders.AUTHORIZATION, admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\": \"Actual#123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("newPassword: must not be blank"));
        verifyNoInteractions(changePasswordUseCase);
    }

    private String admin() {
        return "Bearer " + TestJwt.token(jwtEncoder, TestJwt.ISSUER, Instant.now().plusSeconds(60), "ADMIN");
    }
}
