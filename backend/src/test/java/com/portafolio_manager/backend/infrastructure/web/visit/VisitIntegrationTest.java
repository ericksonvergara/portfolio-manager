package com.portafolio_manager.backend.infrastructure.web.visit;

import com.portafolio_manager.backend.application.port.out.visit.GeoLocationPort;
import com.portafolio_manager.backend.infrastructure.AbstractPostgresIntegrationTest;
import com.portafolio_manager.backend.infrastructure.security.jwt.TestJwt;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Visit registration (HU025) against the real schema.
 */
@SpringBootTest(properties = TestJwt.SECRET_PROPERTY)
@AutoConfigureMockMvc
class VisitIntegrationTest extends AbstractPostgresIntegrationTest {

    private static final String ANDROID_PHONE =
            "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 Chrome/130.0 Mobile Safari/537.36";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtEncoder jwtEncoder;

    @MockitoBean
    private GeoLocationPort geoLocation;

    @AfterEach
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM visits");
        jdbcTemplate.update("DELETE FROM projects");
    }

    @Test
    void shouldStoreEveryDatumOfAVisitWithoutTheFullIp() throws Exception {
        when(geoLocation.countryOf("190.25.14.77")).thenReturn(Optional.of("CO"));

        mockMvc.perform(post("/api/public/visits")
                        .header("X-Real-IP", "190.25.14.77")
                        .header(HttpHeaders.USER_AGENT, ANDROID_PHONE))
                .andExpect(status().isAccepted());

        Map<String, Object> visit = jdbcTemplate.queryForMap("SELECT * FROM visits");
        assertThat(visit.get("visited_at")).isNotNull();
        assertThat(visit.get("anonymized_ip")).isEqualTo("190.25.14.0");
        assertThat(visit.get("browser")).isEqualTo("Chrome");
        assertThat(visit.get("operating_system")).isEqualTo("Android");
        assertThat(visit.get("device")).isEqualTo("Mobile");
        assertThat(visit.get("country")).isEqualTo("CO");
        assertThat(visit.get("project_id")).isNull();
        assertThat(visit.toString()).doesNotContain("190.25.14.77");
    }

    @Test
    void shouldKeepTheVisitOfAProjectThatIsDeleted() throws Exception {
        long project = insertProject("sistema-erp", true);

        visitProject("sistema-erp", "190.25.14.79");
        assertThat(jdbcTemplate.queryForObject("SELECT project_id FROM visits", Long.class)).isEqualTo(project);
        assertThat(jdbcTemplate.queryForObject("SELECT country FROM visits", String.class)).isNull();

        jdbcTemplate.update("DELETE FROM projects WHERE id = ?", project);

        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM visits", Long.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT project_id FROM visits", Long.class)).isNull();
    }

    @Test
    void shouldCountTheVisitsToTheDetailOfEachProject() throws Exception {
        long erp = insertProject("sistema-erp", true);
        long shop = insertProject("tienda", true);
        insertProject("sin-visitas", true);
        when(geoLocation.countryOf("190.25.14.77")).thenReturn(Optional.empty());

        visitProject("sistema-erp", "190.25.14.77");
        visitProject("sistema-erp", "190.25.14.78");
        visitProject("tienda", "190.25.14.77");
        // A visit to the landing page belongs to no project.
        mockMvc.perform(post("/api/public/visits")).andExpect(status().isAccepted());

        mockMvc.perform(get("/api/admin/projects/" + erp).header(HttpHeaders.AUTHORIZATION, admin()))
                .andExpect(jsonPath("$.visitCount").value(2));
        mockMvc.perform(get("/api/admin/projects/" + shop).header(HttpHeaders.AUTHORIZATION, admin()))
                .andExpect(jsonPath("$.visitCount").value(1));
        mockMvc.perform(get("/api/admin/projects").header(HttpHeaders.AUTHORIZATION, admin()))
                .andExpect(jsonPath("$[?(@.slug == 'sistema-erp')].visitCount").value(contains(2)))
                .andExpect(jsonPath("$[?(@.slug == 'tienda')].visitCount").value(contains(1)))
                .andExpect(jsonPath("$[?(@.slug == 'sin-visitas')].visitCount").value(contains(0)));
    }

    @Test
    void shouldNotCountAReloadOfTheSamePage() throws Exception {
        long erp = insertProject("erp-recargado", true);

        visitProject("erp-recargado", "198.51.100.7");
        visitProject("erp-recargado", "198.51.100.7");
        visitProject("erp-recargado", "198.51.100.7");
        // The same visitor on another page, and another visitor on the same page, are new visits.
        mockMvc.perform(post("/api/public/visits").header("X-Real-IP", "198.51.100.7")).andExpect(status().isAccepted());
        visitProject("erp-recargado", "198.51.100.8");

        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM visits", Long.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM visits WHERE project_id = ?", Long.class, erp)).isEqualTo(2);
    }

    @Test
    void shouldNotCountAVisitToAnUnpublishedProject() throws Exception {
        insertProject("oculto", false);

        mockMvc.perform(post("/api/public/visits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"projectSlug\": \"oculto\" }"))
                .andExpect(status().isNotFound());

        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM visits", Long.class)).isZero();
    }

    private void visitProject(String slug, String ip) throws Exception {
        mockMvc.perform(post("/api/public/visits")
                        .header("X-Real-IP", ip)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"projectSlug\": \"" + slug + "\" }"))
                .andExpect(status().isAccepted());
    }

    private String admin() {
        return "Bearer " + TestJwt.token(jwtEncoder, TestJwt.ISSUER, Instant.now().plusSeconds(60), "ADMIN");
    }

    private long insertProject(String slug, boolean active) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO projects (name, slug, short_description, description, cover_image_url, active)
                VALUES (?, ?, 'Corta', 'Completa', '/images/cover.png', ?)
                RETURNING id
                """, Long.class, slug, slug, active);
    }
}
