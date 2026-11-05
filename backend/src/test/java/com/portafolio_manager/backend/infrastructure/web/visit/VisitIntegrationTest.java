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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        when(geoLocation.countryOf("190.25.14.77")).thenReturn(Optional.empty());

        visitProject("sistema-erp");
        assertThat(jdbcTemplate.queryForObject("SELECT project_id FROM visits", Long.class)).isEqualTo(project);
        assertThat(jdbcTemplate.queryForObject("SELECT country FROM visits", String.class)).isNull();

        jdbcTemplate.update("DELETE FROM projects WHERE id = ?", project);

        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM visits", Long.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT project_id FROM visits", Long.class)).isNull();
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

    private void visitProject(String slug) throws Exception {
        mockMvc.perform(post("/api/public/visits")
                        .header("X-Real-IP", "190.25.14.77")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"projectSlug\": \"" + slug + "\" }"))
                .andExpect(status().isAccepted());
    }

    private long insertProject(String slug, boolean active) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO projects (name, slug, short_description, description, cover_image_url, active)
                VALUES (?, ?, 'Corta', 'Completa', '/images/cover.png', ?)
                RETURNING id
                """, Long.class, slug, slug, active);
    }
}
