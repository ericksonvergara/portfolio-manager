package com.portafolio_manager.backend.infrastructure.web.admin.project;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Project registration (HU015) and edition (HU016) against the real schema.
 */
@SpringBootTest(properties = TestJwt.SECRET_PROPERTY)
@AutoConfigureMockMvc
class AdminProjectIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
        create("GESTIÓN DE INVENTARIO", java).andExpect(status().isConflict());
        create("Gestion de inventario", java).andExpect(status().isConflict());
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
}
