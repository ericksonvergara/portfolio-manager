package com.portafolio_manager.backend.infrastructure.web.auth;

import com.portafolio_manager.backend.application.port.out.auth.PasswordHasherPort;
import com.portafolio_manager.backend.application.port.out.auth.PasswordResetMailerPort;
import com.portafolio_manager.backend.application.port.out.auth.RefreshTokenGeneratorPort;
import com.portafolio_manager.backend.infrastructure.AbstractPostgresIntegrationTest;
import com.portafolio_manager.backend.infrastructure.security.jwt.TestJwt;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Password recovery (HU010) against the real schema. The mailer is replaced so the test can read
 * the link that would be emailed.
 */
@SpringBootTest(properties = TestJwt.SECRET_PROPERTY)
@AutoConfigureMockMvc
class PasswordResetIntegrationTest extends AbstractPostgresIntegrationTest {

    private static final String EMAIL = "recovery@portfolio.test";
    private static final String OLD_PASSWORD = "Antigua#1";
    private static final String NEW_PASSWORD = "Nueva#456";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordHasherPort passwordHasher;

    @Autowired
    private RefreshTokenGeneratorPort tokenGenerator;

    @MockitoBean
    private PasswordResetMailerPort mailer;

    @BeforeEach
    void createUser() {
        Long userId = jdbcTemplate.queryForObject(
                "INSERT INTO users (name, email, password_hash) VALUES ('Recovery', ?, ?) RETURNING id",
                Long.class, EMAIL, passwordHasher.hash(OLD_PASSWORD));
        jdbcTemplate.update("INSERT INTO user_roles SELECT ?, id FROM roles WHERE name = 'ADMIN'", userId);
    }

    @AfterEach
    void deleteUser() {
        jdbcTemplate.update("DELETE FROM users WHERE email = ?", EMAIL);
    }

    @Test
    void shouldResetThePasswordOnceAndCloseTheOpenSessions() throws Exception {
        Cookie session = login(OLD_PASSWORD).andExpect(status().isOk()).andReturn().getResponse().getCookie("refresh_token");

        // Same answer for an unknown email: nothing is sent.
        request("nadie@portfolio.test").andExpect(status().isAccepted());
        verify(mailer, never()).sendPasswordReset(anyString(), anyString(), anyString(), any());

        request(EMAIL.toUpperCase()).andExpect(status().isAccepted());
        String first = sentToken();
        clearInvocations(mailer);
        request(EMAIL).andExpect(status().isAccepted());
        String second = sentToken();

        // Only the newest link works, and the policy is checked before using it.
        confirm(first, NEW_PASSWORD).andExpect(status().isBadRequest());
        confirm(second, "corta").andExpect(status().isBadRequest());
        confirm(second, NEW_PASSWORD).andExpect(status().isNoContent());
        confirm(second, NEW_PASSWORD).andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/auth/refresh").cookie(session)).andExpect(status().isUnauthorized());
        login(OLD_PASSWORD).andExpect(status().isUnauthorized());
        login(NEW_PASSWORD).andExpect(status().isOk());

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM password_reset_tokens t JOIN users u ON u.id = t.user_id "
                        + "WHERE u.email = ? AND t.used_at IS NULL", Long.class, EMAIL)).isZero();
    }

    @Test
    void shouldRejectAnExpiredLink() throws Exception {
        jdbcTemplate.update("""
                INSERT INTO password_reset_tokens (user_id, token_hash, expires_at)
                SELECT id, ?, now() - INTERVAL '1 minute' FROM users WHERE email = ?
                """, tokenGenerator.hash("caducado"), EMAIL);

        confirm("caducado", NEW_PASSWORD).andExpect(status().isBadRequest());
        login(OLD_PASSWORD).andExpect(status().isOk());
    }

    private String sentToken() {
        ArgumentCaptor<String> link = ArgumentCaptor.forClass(String.class);
        verify(mailer, times(1)).sendPasswordReset(eq(EMAIL), anyString(), link.capture(),
                eq(Duration.ofMinutes(30)));
        assertThat(link.getValue()).startsWith("http://localhost:8081/restablecer-contrasena?token=");
        return URLDecoder.decode(link.getValue().substring(link.getValue().indexOf("=") + 1),
                StandardCharsets.UTF_8);
    }

    private ResultActions request(String email) throws Exception {
        return mockMvc.perform(post("/api/auth/password-reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"" + email + "\"}"));
    }

    private ResultActions confirm(String token, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/password-reset/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\": \"" + token + "\", \"newPassword\": \"" + password + "\"}"));
    }

    private ResultActions login(String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"" + EMAIL + "\", \"password\": \"" + password + "\"}"));
    }
}
