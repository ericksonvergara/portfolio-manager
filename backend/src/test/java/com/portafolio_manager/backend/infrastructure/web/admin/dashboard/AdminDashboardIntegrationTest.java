package com.portafolio_manager.backend.infrastructure.web.admin.dashboard;

import com.jayway.jsonpath.JsonPath;
import com.portafolio_manager.backend.application.port.out.auth.PasswordHasherPort;
import com.portafolio_manager.backend.infrastructure.AbstractPostgresIntegrationTest;
import com.portafolio_manager.backend.infrastructure.security.jwt.TestJwt;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.sql.Timestamp;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The dashboard (HU012) against the real schema, with real logins for the last access.
 */
@SpringBootTest(properties = TestJwt.SECRET_PROPERTY)
@AutoConfigureMockMvc
class AdminDashboardIntegrationTest extends AbstractPostgresIntegrationTest {

    private static final String EMAIL = "dashboard@portfolio.test";
    private static final String PASSWORD = "Tablero#123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordHasherPort passwordHasher;

    @Autowired
    private JwtEncoder jwtEncoder;

    @BeforeEach
    void createAdministrator() {
        Long userId = jdbcTemplate.queryForObject(
                "INSERT INTO users (name, email, password_hash) VALUES ('Dashboard', ?, ?) RETURNING id",
                Long.class, EMAIL, passwordHasher.hash(PASSWORD));
        jdbcTemplate.update("INSERT INTO user_roles SELECT ?, id FROM roles WHERE name = 'ADMIN'", userId);
    }

    @AfterEach
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM users WHERE email = ?", EMAIL);
        jdbcTemplate.update("DELETE FROM projects");
        jdbcTemplate.update("DELETE FROM technologies");
    }

    @Test
    void shouldCountEveryProjectAndTechnologyAndShowTheLatestProject() throws Exception {
        insertProject("Antiguo", true, "2026-01-10T10:00:00Z");
        insertProject("Reciente oculto", false, "2026-11-02T15:30:00Z");
        jdbcTemplate.update("INSERT INTO technologies (name, category, display_order) VALUES ('Java', 'BACKEND', 1)");

        dashboard(login())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProjects").value(2))
                .andExpect(jsonPath("$.totalTechnologies").value(1))
                .andExpect(jsonPath("$.latestProject.name").value("Reciente oculto"))
                .andExpect(jsonPath("$.latestProject.registeredAt").value("2026-11-02T15:30:00Z"));
    }

    @Test
    void shouldWorkOnAnEmptyPortfolio() throws Exception {
        dashboard(login())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProjects").value(0))
                .andExpect(jsonPath("$.latestProject").doesNotExist());
    }

    @Test
    void shouldShowTheAccessBeforeTheCurrentSession() throws Exception {
        String first = login();
        dashboard(first).andExpect(jsonPath("$.lastAccessAt").doesNotExist());

        Instant firstLogin = lastLogin();
        String second = login();

        dashboard(second).andExpect(jsonPath("$.lastAccessAt").value(firstLogin.toString()));
    }

    @Test
    void shouldKeepTheLastAccessWhenTheUserIsSaved() throws Exception {
        login();
        Instant firstLogin = lastLogin();
        String token = login();

        // Saving the user rewrites the entity: the unmapped column must survive it.
        mockMvc.perform(put("/api/admin/account/password")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"currentPassword\": \"%s\", \"newPassword\": \"Tablero#456\" }".formatted(PASSWORD)))
                .andExpect(status().isNoContent());

        assertThat(jdbcTemplate.queryForObject(
                "SELECT previous_login_at FROM users WHERE email = ?", Timestamp.class, EMAIL).toInstant())
                .isEqualTo(firstLogin);
    }

    @Test
    void shouldKeepTheDashboardForTheAdministrator() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/dashboard")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestJwt.token(
                                jwtEncoder, TestJwt.ISSUER, Instant.now().plusSeconds(60), "USER")))
                .andExpect(status().isForbidden());
    }

    private ResultActions dashboard(String token) throws Exception {
        return mockMvc.perform(get("/api/admin/dashboard").header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }

    private String login() throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"email\": \"%s\", \"password\": \"%s\" }".formatted(EMAIL, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.accessToken");
    }

    private Instant lastLogin() {
        return jdbcTemplate.queryForObject(
                "SELECT last_login_at FROM users WHERE email = ?", Timestamp.class, EMAIL).toInstant();
    }

    private void insertProject(String name, boolean active, String createdAt) {
        jdbcTemplate.update("""
                INSERT INTO projects (name, slug, short_description, description, cover_image_url, active, created_at)
                VALUES (?, lower(replace(?, ' ', '-')), 'Corta', 'Completa', '/images/cover.png', ?, ?::timestamptz)
                """, name, name, active, createdAt);
    }
}
