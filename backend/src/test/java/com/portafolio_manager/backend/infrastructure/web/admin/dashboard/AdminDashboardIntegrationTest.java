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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
        jdbcTemplate.update("DELETE FROM visits");
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
    void shouldShowTheVisitStatistics() throws Exception {
        long erp = insertProject("Sistema ERP", true, "2026-01-10T10:00:00Z");
        long shop = insertProject("Tienda", true, "2026-01-11T10:00:00Z");
        insertVisit(erp, "now()");
        insertVisit(erp, "now()");
        insertVisit(shop, "now()");
        insertVisit(null, "now()");
        insertVisit(shop, "now() - interval '70 days'");
        insertVisit(shop, "now() - interval '71 days'");
        insertVisit(shop, "now() - interval '72 days'");

        dashboard(login())
                .andExpect(jsonPath("$.visits.total").value(7))
                .andExpect(jsonPath("$.visits.today").value(4))
                .andExpect(jsonPath("$.visits.thisMonth").value(4))
                .andExpect(jsonPath("$.visits.mostVisitedProject.name").value("Tienda"))
                .andExpect(jsonPath("$.visits.mostVisitedProject.visits").value(4));
    }

    @Test
    void shouldNotNameAMostVisitedProjectWithoutProjectVisits() throws Exception {
        insertVisit(null, "now()");

        dashboard(login())
                .andExpect(jsonPath("$.visits.total").value(1))
                .andExpect(jsonPath("$.visits.mostVisitedProject").doesNotExist());
    }

    @Test
    void shouldShowTheLatestProjectStillPublished() throws Exception {
        long old = insertProject("Antiguo", true, "2026-01-10T10:00:00Z");
        long hidden = insertProject("Oculto", false, "2026-01-11T10:00:00Z");
        jdbcTemplate.update("UPDATE projects SET updated_at = '2026-02-01T10:00:00Z' WHERE id = ?", old);
        jdbcTemplate.update("UPDATE projects SET published_at = now() WHERE id = ?", hidden);
        String token = login();

        // Published before the column existed: updated_at stands in for the date.
        dashboard(token)
                .andExpect(jsonPath("$.latestPublishedProject.name").value("Antiguo"))
                .andExpect(jsonPath("$.latestPublishedProject.publishedAt").value("2026-02-01T10:00:00Z"));

        long technology = jdbcTemplate.queryForObject(
                "INSERT INTO technologies (name, category, display_order) VALUES ('Java', 'BACKEND', 1) RETURNING id",
                Long.class);
        jdbcTemplate.update("INSERT INTO project_technologies VALUES (?, ?)", hidden, technology);
        mockMvc.perform(patch("/api/admin/projects/" + hidden + "/publication")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"active\": true }"))
                .andExpect(status().isOk());

        dashboard(token).andExpect(jsonPath("$.latestPublishedProject.name").value("Oculto"));
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

    private long insertProject(String name, boolean active, String createdAt) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO projects (name, slug, short_description, description, cover_image_url, active, created_at)
                VALUES (?, lower(replace(?, ' ', '-')), 'Corta', 'Completa', '/images/cover.png', ?, ?::timestamptz)
                RETURNING id
                """, Long.class, name, name, active, createdAt);
    }

    private void insertVisit(Long projectId, String visitedAt) {
        jdbcTemplate.update("""
                INSERT INTO visits (visited_at, anonymized_ip, browser, operating_system, device, project_id)
                VALUES (%s, '190.25.14.0', 'Chrome', 'Windows', 'Desktop', ?)
                """.formatted(visitedAt), projectId);
    }
}
