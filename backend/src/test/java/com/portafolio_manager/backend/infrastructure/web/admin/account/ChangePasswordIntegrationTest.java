package com.portafolio_manager.backend.infrastructure.web.admin.account;

import com.jayway.jsonpath.JsonPath;
import com.portafolio_manager.backend.application.port.out.auth.PasswordHasherPort;
import com.portafolio_manager.backend.infrastructure.AbstractPostgresIntegrationTest;
import com.portafolio_manager.backend.infrastructure.security.jwt.TestJwt;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Password change (HU011) against the real schema, with a user of its own so the other tests keep
 * their credentials.
 */
@SpringBootTest(properties = TestJwt.SECRET_PROPERTY)
@AutoConfigureMockMvc
class ChangePasswordIntegrationTest extends AbstractPostgresIntegrationTest {

    private static final String EMAIL = "change@portfolio.test";
    private static final String CURRENT = "Actual#123";
    private static final String NEW = "Nueva#456";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordHasherPort passwordHasher;

    @BeforeEach
    void createUser() {
        Long userId = jdbcTemplate.queryForObject(
                "INSERT INTO users (name, email, password_hash) VALUES ('Change', ?, ?) RETURNING id",
                Long.class, EMAIL, passwordHasher.hash(CURRENT));
        jdbcTemplate.update("INSERT INTO user_roles SELECT ?, id FROM roles WHERE name = 'ADMIN'", userId);
    }

    @AfterEach
    void deleteUser() {
        jdbcTemplate.update("DELETE FROM users WHERE email = ?", EMAIL);
    }

    @Test
    void shouldChangeThePasswordAndCloseEverySession() throws Exception {
        MvcResult session = login(CURRENT).andExpect(status().isOk()).andReturn();
        String accessToken = JsonPath.read(session.getResponse().getContentAsString(), "$.accessToken");
        Cookie refresh = session.getResponse().getCookie("refresh_token");
        Cookie otherDevice = login(CURRENT).andReturn().getResponse().getCookie("refresh_token");

        change(accessToken, "Equivocada#1", NEW).andExpect(status().isBadRequest());
        change(accessToken, CURRENT, "corta").andExpect(status().isBadRequest());
        change(accessToken, CURRENT, CURRENT).andExpect(status().isBadRequest());
        change(accessToken, CURRENT, NEW).andExpect(status().isNoContent());

        String hash = jdbcTemplate.queryForObject("SELECT password_hash FROM users WHERE email = ?", String.class, EMAIL);
        assertThat(hash).startsWith("$2"); // BCrypt
        assertThat(passwordHasher.matches(NEW, hash)).isTrue();

        // Every session is closed, the one of the request included.
        mockMvc.perform(get("/api/admin/projects").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/refresh").cookie(refresh)).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/refresh").cookie(otherDevice)).andExpect(status().isUnauthorized());

        login(CURRENT).andExpect(status().isUnauthorized());
        login(NEW).andExpect(status().isOk());
    }

    private ResultActions change(String accessToken, String current, String next) throws Exception {
        return mockMvc.perform(put("/api/admin/account/password")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\": \"" + current + "\", \"newPassword\": \"" + next + "\"}"));
    }

    private ResultActions login(String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"" + EMAIL + "\", \"password\": \"" + password + "\"}"));
    }
}
