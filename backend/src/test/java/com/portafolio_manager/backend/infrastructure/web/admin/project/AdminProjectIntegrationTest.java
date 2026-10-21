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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Project registration (HU015) against the real schema.
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

    private ResultActions create(String name, long... technologyIds) throws Exception {
        String ids = String.join(", ", Arrays.stream(technologyIds).mapToObj(String::valueOf).toList());
        return mockMvc.perform(post("/api/admin/projects")
                .header(HttpHeaders.AUTHORIZATION, adminToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
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
                        """.formatted(name, ids)));
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
