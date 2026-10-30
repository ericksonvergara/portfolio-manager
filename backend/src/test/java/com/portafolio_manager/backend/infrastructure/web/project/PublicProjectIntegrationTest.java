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
    void shouldSearchThePublishedProjectsByNameOrTechnology() throws Exception {
        long erp = insertProject("Gestión de Imágenes", true, false, 1);
        long shop = insertProject("Tienda en línea", true, false, 2);
        insertProject("Imágenes ocultas", false, false, 3);
        long java = insertTechnology("Java", 1);
        long react = insertTechnology("React", 2);
        jdbcTemplate.update("INSERT INTO project_technologies VALUES (?, ?), (?, ?), (?, ?)",
                erp, java, shop, java, shop, react);

        mockMvc.perform(get("/api/public/projects").param("search", "IMAGENES"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name").value(contains("Gestión de Imágenes")));
        mockMvc.perform(get("/api/public/projects").param("search", "java"))
                .andExpect(jsonPath("$[*].name").value(contains("Gestión de Imágenes", "Tienda en línea")));
        mockMvc.perform(get("/api/public/projects").param("search", "java react"))
                .andExpect(jsonPath("$[*].name").value(contains("Tienda en línea")));
        mockMvc.perform(get("/api/public/projects").param("search", "python"))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldFilterThePublishedProjectsByTheCategoryOfTheirTechnologies() throws Exception {
        long api = insertProject("API de pagos", true, false, 1);
        long web = insertProject("Tienda web", true, false, 2);
        long bot = insertProject("Asistente", true, false, 3);
        long java = insertTechnology("Java", "BACKEND", 1);
        long react = insertTechnology("React", "FRONTEND", 2);
        long openAi = insertTechnology("OpenAI", "AI", 3); // V7: the AI category exists
        jdbcTemplate.update("INSERT INTO project_technologies VALUES (?, ?), (?, ?), (?, ?), (?, ?)",
                api, java, web, java, web, react, bot, openAi);

        mockMvc.perform(get("/api/public/projects").param("category", "BACKEND"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name").value(contains("API de pagos", "Tienda web")));
        mockMvc.perform(get("/api/public/projects").param("category", "FULL_STACK"))
                .andExpect(jsonPath("$[*].name").value(contains("Tienda web")));
        mockMvc.perform(get("/api/public/projects").param("category", "AI"))
                .andExpect(jsonPath("$[*].name").value(contains("Asistente")));
        mockMvc.perform(get("/api/public/projects").param("category", "BACKEND").param("search", "pagos"))
                .andExpect(jsonPath("$[*].name").value(contains("API de pagos")));
        mockMvc.perform(get("/api/public/projects").param("category", "MOBILE"))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldReturnTheDetailOfAnActiveProject() throws Exception {
        long project = insertProject("Sistema ERP", true, false, 1);
        jdbcTemplate.update("""
                UPDATE projects
                SET project_url = 'https://erp.example.com', video_url = 'https://youtu.be/abc123',
                    status = 'IN_PROGRESS', developed_on = DATE '2026-10-01'
                WHERE id = ?
                """, project);
        jdbcTemplate.update("""
                INSERT INTO project_images (project_id, image_url, alt_text, display_order)
                VALUES (?, '/images/second.png', NULL, 2), (?, '/images/first.png', 'Pantalla principal', 1)
                """, project, project);
        long java = insertTechnology("Java", 1);
        jdbcTemplate.update("INSERT INTO project_technologies VALUES (?, ?)", project, java);

        jdbcTemplate.update("UPDATE projects SET slug = 'sistema-erp' WHERE id = ?", project);

        mockMvc.perform(get("/api/public/projects/sistema-erp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sistema ERP"))
                .andExpect(jsonPath("$.description").value("Descripción completa"))
                .andExpect(jsonPath("$.projectUrl").value("https://erp.example.com"))
                .andExpect(jsonPath("$.repositoryUrl").doesNotExist())
                .andExpect(jsonPath("$.videoUrl").value("https://youtu.be/abc123"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.developedOn").value("2026-10-01"))
                .andExpect(jsonPath("$.gallery[*].url").value(contains("/images/first.png", "/images/second.png")))
                .andExpect(jsonPath("$.gallery[0].altText").value("Pantalla principal"))
                .andExpect(jsonPath("$.technologies").value(contains("Java")));
    }

    @Test
    void shouldHideInactiveAndUnknownProjects() throws Exception {
        insertProject("Oculto", false, false, 1);

        mockMvc.perform(get("/api/public/projects/oculto"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
        mockMvc.perform(get("/api/public/projects/no-existe"))
                .andExpect(status().isNotFound());
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
        return insertTechnology(name, "BACKEND", displayOrder);
    }

    private long insertTechnology(String name, String category, int displayOrder) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO technologies (name, category, display_order) VALUES (?, ?, ?) RETURNING id",
                Long.class, name, category, displayOrder);
    }
}
