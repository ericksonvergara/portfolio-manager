package com.portafolio_manager.backend.infrastructure.web.auth;

import com.portafolio_manager.backend.application.port.in.auth.AuthenticationResult;
import com.portafolio_manager.backend.application.port.in.auth.LoginCommand;
import com.portafolio_manager.backend.application.port.in.auth.LoginUseCase;
import com.portafolio_manager.backend.application.port.in.auth.LogoutCommand;
import com.portafolio_manager.backend.application.port.in.auth.LogoutUseCase;
import com.portafolio_manager.backend.application.port.in.auth.RequestPasswordResetUseCase;
import com.portafolio_manager.backend.application.port.in.auth.ResetPasswordCommand;
import com.portafolio_manager.backend.application.port.in.auth.ResetPasswordUseCase;
import com.portafolio_manager.backend.domain.auth.InvalidPasswordResetTokenException;
import com.portafolio_manager.backend.domain.user.WeakPasswordException;
import com.portafolio_manager.backend.application.port.in.auth.RefreshSessionUseCase;
import com.portafolio_manager.backend.application.port.out.auth.RevokedAccessTokenRepositoryPort;
import com.portafolio_manager.backend.domain.auth.InvalidCredentialsException;
import com.portafolio_manager.backend.domain.auth.InvalidRefreshTokenException;
import com.portafolio_manager.backend.infrastructure.security.SecurityConfig;
import com.portafolio_manager.backend.infrastructure.security.jwt.TestJwt;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class, properties = TestJwt.SECRET_PROPERTY)
@Import({SecurityConfig.class, RefreshTokenCookieFactory.class, AuthControllerTest.FixedClockConfig.class})
class AuthControllerTest {

    private static final Instant NOW = Instant.parse("2026-10-09T12:00:00Z");
    private static final AuthenticationResult SESSION = new AuthenticationResult(
            "access-token",
            NOW.plusSeconds(900),
            "refresh-token",
            NOW.plusSeconds(3600)
    );

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoginUseCase loginUseCase;

    @MockitoBean
    private RefreshSessionUseCase refreshSessionUseCase;

    @MockitoBean
    private LogoutUseCase logoutUseCase;

    @MockitoBean
    private RequestPasswordResetUseCase requestPasswordResetUseCase;

    @MockitoBean
    private ResetPasswordUseCase resetPasswordUseCase;

    @MockitoBean
    private RevokedAccessTokenRepositoryPort revokedAccessTokenRepository;

    @Test
    void shouldReturnAccessTokenAndSetRefreshCookieOnLogin() throws Exception {
        when(loginUseCase.execute(new LoginCommand("admin@example.com", "Secret#1"))).thenReturn(SESSION);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "admin@example.com", "password": "Secret#1"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, allOf(
                        containsString("refresh_token=refresh-token"),
                        containsString("Path=/api/auth"),
                        containsString("Max-Age=3600"),
                        containsString("HttpOnly"),
                        containsString("Secure"),
                        containsString("SameSite=Strict")
                )));
    }

    @Test
    void shouldReturnBadRequestForInvalidLoginBody() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "not-an-email", "password": ""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(allOf(containsString("email"), containsString("password"))));

        verifyNoInteractions(loginUseCase);
    }

    @Test
    void shouldReturnUnauthorizedForInvalidCredentials() throws Exception {
        when(loginUseCase.execute(any())).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "admin@example.com", "password": "Wrong#1"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void shouldRotateSessionUsingRefreshCookie() throws Exception {
        when(refreshSessionUseCase.execute("old-refresh")).thenReturn(SESSION);

        mockMvc.perform(post("/api/auth/refresh").cookie(new Cookie("refresh_token", "old-refresh")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refresh_token=refresh-token")));
    }

    @Test
    void shouldReturnUnauthorizedWhenRefreshCookieIsInvalid() throws Exception {
        when(refreshSessionUseCase.execute(null)).thenThrow(new InvalidRefreshTokenException());

        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRevokeBothTokensAndClearCookieOnLogout() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .cookie(new Cookie("refresh_token", "current-refresh"))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer current-access"))
                .andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, allOf(
                        containsString("refresh_token="),
                        containsString("Max-Age=0")
                )));

        verify(logoutUseCase).execute(new LogoutCommand("current-refresh", "current-access"));
    }

    @Test
    void shouldLogoutWithoutAnyToken() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isNoContent());

        verify(logoutUseCase).execute(new LogoutCommand(null, null));
    }

    @TestConfiguration
    static class FixedClockConfig {

        @Bean
        Clock clock() {
            return Clock.fixed(NOW, ZoneOffset.UTC);
        }
    }

    @Test
    void shouldAcceptAPasswordResetRequestWithoutRevealingTheAccount() throws Exception {
        mockMvc.perform(post("/api/auth/password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"admin@example.com\"}"))
                .andExpect(status().isAccepted())
                .andExpect(content().string(""));
        verify(requestPasswordResetUseCase).execute("admin@example.com");

        mockMvc.perform(post("/api/auth/password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"no-es-correo\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldSetTheNewPasswordFromTheLink() throws Exception {
        mockMvc.perform(post("/api/auth/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\": \"abc\", \"newPassword\": \"Nueva#123\"}"))
                .andExpect(status().isNoContent());
        verify(resetPasswordUseCase).execute(new ResetPasswordCommand("abc", "Nueva#123"));
    }

    @Test
    void shouldRejectAnInvalidLinkOrAWeakPassword() throws Exception {
        doThrow(new InvalidPasswordResetTokenException())
                .when(resetPasswordUseCase).execute(new ResetPasswordCommand("viejo", "Nueva#123"));
        doThrow(new WeakPasswordException())
                .when(resetPasswordUseCase).execute(new ResetPasswordCommand("abc", "corta"));

        mockMvc.perform(post("/api/auth/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\": \"viejo\", \"newPassword\": \"Nueva#123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("The password reset link is invalid or has expired"));
        mockMvc.perform(post("/api/auth/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\": \"abc\", \"newPassword\": \"corta\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(startsWith("The password must have")));
    }
}
