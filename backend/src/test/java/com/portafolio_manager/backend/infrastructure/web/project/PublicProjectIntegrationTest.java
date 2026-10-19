package com.portafolio_manager.backend.infrastructure.web.project;

import com.portafolio_manager.backend.infrastructure.AbstractPostgresIntegrationTest;
import com.portafolio_manager.backend.infrastructure.security.jwt.TestJwt;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The public project listing against the real schema (HU002).
 */
@SpringBootTest(properties = TestJwt.SECRET_PROPERTY)
@AutoConfigureMockMvc
class PublicProjectIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM projects");
        jdbcTemplate.update("DELETE FROM technologies");
    }

    @Test
    void shouldListOnlyActiveProjectsFeaturedFirstThenByDisplayOrder() throws Exception {
        mockMvc.perform(get("/api/public/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        long second = insertProject("Segundo", true, false, 2);
        long featured = insertProject("Destacado", true, true, 9);
        insertProject("Oculto", false, true, 1);
        insertProject("Primero", true, false, 1);

        long java = insertTechnology("Java", 2);
        long react = insertTechnology("React", 1);
        jdbcTemplate.update("INSERT INTO project_technologies VALUES (?, ?), (?, ?)", featured, java, featured, react);

        mockMvc.perform(get("/api/public/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name").value(contains("Destacado", "Primero", "Segundo")))
                .andExpect(jsonPath("$[0].slug").value("destacado"))
                .andExpect(jsonPath("$[0].technologies").value(contains("React", "Java")))
                .andExpect(jsonPath("$[1].technologies", hasSize(0)));

        // Deactivating a project removes it from the landing page (RN008).
        jdbcTemplate.update("UPDATE projects SET active = FALSE WHERE id = ?", second);
        mockMvc.perform(get("/api/public/projects"))
                .andExpect(jsonPath("$[*].name").value(contains("Destacado", "Primero")));
    }

    @Test
    void shouldRejectPublishingAProjectWithoutCoverImage() {
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO projects (name, slug, short_description, description, active)
                VALUES ('Sin portada', 'sin-portada', 'Corta', 'Completa', TRUE)
                """))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldRejectTwoProjectsWithTheSameName() {
        insertProject("Repetido", false, false, 1);

        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO projects (name, slug, short_description, description)
                VALUES ('Repetido', 'repetido-2', 'Corta', 'Completa')
                """))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private long insertProject(String name, boolean active, boolean featured, int displayOrder) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO projects (name, slug, short_description, description, cover_image_url,
                                      active, featured, display_order)
                VALUES (?, lower(?), 'Descripción corta', 'Descripción completa', '/images/cover.png', ?, ?, ?)
                RETURNING id
                """, Long.class, name, name, active, featured, displayOrder);
    }

    private long insertTechnology(String name, int displayOrder) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO technologies (name, category, display_order) VALUES (?, 'BACKEND', ?) RETURNING id",
                Long.class, name, displayOrder);
    }
}
