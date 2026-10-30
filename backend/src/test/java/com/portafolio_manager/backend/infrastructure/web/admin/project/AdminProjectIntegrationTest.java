package com.portafolio_manager.backend.infrastructure.web.admin.project;

import com.portafolio_manager.backend.application.port.out.project.ProjectRepositoryPort;
import com.portafolio_manager.backend.domain.project.DuplicateProjectNameException;
import com.portafolio_manager.backend.domain.project.ProjectDraft;
import com.portafolio_manager.backend.domain.project.ProjectStatus;
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

import java.time.Instant;
import java.util.Arrays;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Project registration (HU015), edition (HU016), deletion (HU017) and publication (HU018)
 * against the real schema.
 */
@SpringBootTest(properties = TestJwt.SECRET_PROPERTY)
@AutoConfigureMockMvc
class AdminProjectIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ProjectRepositoryPort projectRepository;

    @Autowired
    private JwtEncoder jwtEncoder;

    private long java;
    private long react;

    @BeforeEach
    void createCatalog() {
        java = insertTechnology("Java", 2);
        react = insertTechnology("React", 1);
    }

    @AfterEach
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM projects");
        jdbcTemplate.update("DELETE FROM technologies");
    }

    @Test
    void shouldCreateAnInactiveProjectThatTheLandingPageDoesNotShow() throws Exception {
        create("Gestión de Inventario", java, react)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("gestion-de-inventario"))
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.coverImageUrl").doesNotExist())
                .andExpect(jsonPath("$.technologies[*].name").value(contains("React", "Java")));

        mockMvc.perform(get("/api/admin/projects").header(HttpHeaders.AUTHORIZATION, adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Gestión de Inventario"));

        // RN008: inactive, so not published
        mockMvc.perform(get("/api/public/projects")).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldRejectADuplicatedNameIgnoringCaseAndAccents() throws Exception {
        create("Gestión de Inventario", java).andExpect(status().isCreated());

        // RN001: same name in another case, and a name that produces the same slug
        create("GESTIÓN DE INVENTARIO", java)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("A project named 'GESTIÓN DE INVENTARIO', or with an equivalent name, already exists"));
        create("Gestion de inventario", java).andExpect(status().isConflict());
        create("  Gestión   de inventario!  ", java).andExpect(status().isConflict());

        mockMvc.perform(get("/api/admin/projects").header(HttpHeaders.AUTHORIZATION, adminToken()))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldRejectRenamingAProjectToTheNameOfAnother() throws Exception {
        create("Gestión de Inventario", java).andExpect(status().isCreated());
        long erp = createdId("Sistema ERP", java);

        update(erp, "gestion de INVENTARIO", java).andExpect(status().isConflict());
        // Keeping its own name, or changing only its case, is not a duplicate.
        update(erp, "Sistema ERP", java).andExpect(status().isOk());
        update(erp, "SISTEMA ERP", java)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("SISTEMA ERP"));
    }

    @Test
    void shouldReportANameTakenConcurrentlyAsADuplicate() {
        // Skips the service check, as when two requests save the same name at once: the schema decides.
        projectRepository.create(draft("Sistema ERP"));

        assertThatThrownBy(() -> projectRepository.create(draft("SISTEMA ERP")))
                .isInstanceOf(DuplicateProjectNameException.class);
    }

    @Test
    void shouldRejectTechnologiesThatAreNotInTheCatalog() throws Exception {
        create("Sistema ERP", java, 999_999L)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unknown technologies: [999999]"));

        mockMvc.perform(get("/api/admin/projects").header(HttpHeaders.AUTHORIZATION, adminToken()))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldEditAProjectKeepingItsPublicationAndRegeneratingTheSlug() throws Exception {
        long id = createdId("Sistema ERP", java, react);
        jdbcTemplate.update("UPDATE projects SET active = TRUE, cover_image_url = '/uploads/erp.png' WHERE id = ?", id);

        update(id, "Sistema ERP Cloud", react)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sistema ERP Cloud"))
                .andExpect(jsonPath("$.slug").value("sistema-erp-cloud"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.coverImageUrl").value("/uploads/erp.png"))
                .andExpect(jsonPath("$.technologies[*].name").value(contains("React")));

        // The public URL follows the new name.
        mockMvc.perform(get("/api/public/projects/sistema-erp-cloud")).andExpect(status().isOk());
        mockMvc.perform(get("/api/public/projects/sistema-erp")).andExpect(status().isNotFound());

        mockMvc.perform(get("/api/admin/projects/" + id).header(HttpHeaders.AUTHORIZATION, adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sistema ERP Cloud"));
        Boolean touched = jdbcTemplate.queryForObject(
                "SELECT updated_at > created_at FROM projects WHERE id = ?", Boolean.class, id);
        assertThat(touched).isTrue();
    }

    @Test
    void shouldKeepItsOwnNameButRejectTheNameOfAnotherProject() throws Exception {
        long erp = createdId("Sistema ERP", java);
        createdId("Gestión de Inventario", java);

        // RN001 ignores the project being edited...
        update(erp, "SISTEMA ERP", java).andExpect(status().isOk());
        // ...but not the others.
        update(erp, "Gestion de inventario", java).andExpect(status().isConflict());
    }

    @Test
    void shouldAnswerNotFoundForAnUnknownProject() throws Exception {
        mockMvc.perform(get("/api/admin/projects/999999").header(HttpHeaders.AUTHORIZATION, adminToken()))
                .andExpect(status().isNotFound());
        update(999_999L, "Sistema ERP", java).andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteAProjectWithItsGalleryAndTechnologyLinks() throws Exception {
        long id = createdId("Sistema ERP", java, react);
        long kept = createdId("Gestión de Inventario", java);
        jdbcTemplate.update("""
                UPDATE projects SET active = TRUE, slug = 'sistema-erp', cover_image_url = '/uploads/erp.png'
                WHERE id = ?
                """, id);
        jdbcTemplate.update(
                "INSERT INTO project_images (project_id, image_url, display_order) VALUES (?, '/uploads/1.png', 1)", id);

        mockMvc.perform(delete("/api/admin/projects/" + id).header(HttpHeaders.AUTHORIZATION, adminToken()))
                .andExpect(status().isNoContent());

        assertThat(count("SELECT COUNT(*) FROM project_images WHERE project_id = ?", id)).isZero();
        assertThat(count("SELECT COUNT(*) FROM project_technologies WHERE project_id = ?", id)).isZero();
        // The catalog and the other projects stay.
        assertThat(count("SELECT COUNT(*) FROM technologies WHERE id IN (?, ?)", java, react)).isEqualTo(2);
        assertThat(count("SELECT COUNT(*) FROM project_technologies WHERE project_id = ?", kept)).isEqualTo(1);

        mockMvc.perform(get("/api/public/projects/sistema-erp")).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/admin/projects/" + id).header(HttpHeaders.AUTHORIZATION, adminToken()))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/admin/projects/" + id).header(HttpHeaders.AUTHORIZATION, adminToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldPublishAndHideAProjectWithoutDeletingIt() throws Exception {
        long id = createdId("Sistema ERP", java);

        // RN004: no cover image yet
        publish(id, true).andExpect(status().isConflict());

        jdbcTemplate.update("UPDATE projects SET cover_image_url = '/uploads/erp.png' WHERE id = ?", id);
        publish(id, true)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
        mockMvc.perform(get("/api/public/projects/sistema-erp")).andExpect(status().isOk());
        mockMvc.perform(get("/api/public/projects")).andExpect(jsonPath("$[*].name").value(contains("Sistema ERP")));

        // RN008: hidden from the landing page, but still in the admin panel
        publish(id, false)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
        mockMvc.perform(get("/api/public/projects/sistema-erp")).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/public/projects")).andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(get("/api/admin/projects").header(HttpHeaders.AUTHORIZATION, adminToken()))
                .andExpect(jsonPath("$", hasSize(1)));

        publish(999_999L, true).andExpect(status().isNotFound());
    }

    private ResultActions publish(long id, boolean active) throws Exception {
        return mockMvc.perform(patch("/api/admin/projects/" + id + "/publication")
                .header(HttpHeaders.AUTHORIZATION, adminToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"active\": " + active + "}"));
    }

    private long count(String sql, Object... args) {
        return jdbcTemplate.queryForObject(sql, Long.class, args);
    }

    private ResultActions create(String name, long... technologyIds) throws Exception {
        return mockMvc.perform(post("/api/admin/projects")
                .header(HttpHeaders.AUTHORIZATION, adminToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(name, technologyIds)));
    }

    private long createdId(String name, long... technologyIds) throws Exception {
        String response = create(name, technologyIds)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return Long.parseLong(response.replaceAll("(?s).*?\"id\":(\\d+).*", "$1"));
    }

    private ResultActions update(long id, String name, long... technologyIds) throws Exception {
        return mockMvc.perform(put("/api/admin/projects/" + id)
                .header(HttpHeaders.AUTHORIZATION, adminToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(name, technologyIds)));
    }

    private static String body(String name, long... technologyIds) {
        String ids = String.join(", ", Arrays.stream(technologyIds).mapToObj(String::valueOf).toList());
        return """
                {
                  "name": "%s",
                  "shortDescription": "Control de existencias.",
                  "description": "Descripción completa.",
                  "repositoryUrl": "https://github.com/usuario/inventario",
                  "status": "IN_PROGRESS",
                  "displayOrder": 1,
                  "featured": false,
                  "technologyIds": [%s]
                }
                """.formatted(name, ids);
    }

    private String adminToken() {
        return "Bearer " + TestJwt.token(jwtEncoder, TestJwt.ISSUER, Instant.now().plusSeconds(60), "ADMIN");
    }

    private long insertTechnology(String name, int displayOrder) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO technologies (name, category, display_order) VALUES (?, 'BACKEND', ?) RETURNING id",
                Long.class, name, displayOrder);
    }

    private ProjectDraft draft(String name) {
        return new ProjectDraft(name, "Corta", "Completa", null, null, null, ProjectStatus.COMPLETED, null, 1, false,
                Set.of(java));
    }
}
