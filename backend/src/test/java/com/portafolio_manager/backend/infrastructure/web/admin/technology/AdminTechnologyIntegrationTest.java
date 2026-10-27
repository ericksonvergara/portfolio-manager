package com.portafolio_manager.backend.infrastructure.web.admin.technology;

import com.jayway.jsonpath.JsonPath;
import com.portafolio_manager.backend.infrastructure.AbstractPostgresIntegrationTest;
import com.portafolio_manager.backend.infrastructure.security.jwt.TestJwt;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Technology catalog management (HU021) against the real schema.
 */
@SpringBootTest(properties = TestJwt.SECRET_PROPERTY)
@AutoConfigureMockMvc
class AdminTechnologyIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtEncoder jwtEncoder;

    @AfterEach
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM projects");
        jdbcTemplate.update("DELETE FROM technologies");
    }

    @Test
    void shouldCreateEditListAndDeleteTechnologies() throws Exception {
        long react = id(save("React", "FRONTEND", 2).andExpect(status().isCreated()));
        long java = id(save("Java", "BACKEND", 1));

        mockMvc.perform(put("/api/admin/technologies/" + react).header(HttpHeaders.AUTHORIZATION, admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"React 19\", \"category\": \"FRONTEND\", \"displayOrder\": 3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("React 19"));

        mockMvc.perform(get("/api/admin/technologies").header(HttpHeaders.AUTHORIZATION, admin()))
                .andExpect(jsonPath("$[*].name").value(contains("Java", "React 19")))
                .andExpect(jsonPath("$[0].projectCount").value(0));
        // The public catalog of the landing page is the same one.
        mockMvc.perform(get("/api/public/technologies")).andExpect(jsonPath("$[*].name").value(contains("Java", "React 19")));

        remove(java).andExpect(status().isNoContent());
        remove(java).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/public/technologies")).andExpect(jsonPath("$[*].name").value(contains("React 19")));
    }

    @Test
    void shouldRejectDuplicatedNamesIgnoringCase() throws Exception {
        long java = id(save("Java", "BACKEND", 1));
        long kotlin = id(save("Kotlin", "BACKEND", 2));

        save("JAVA", "BACKEND", 3).andExpect(status().isConflict());
        // Renaming another technology to an existing name is rejected too; keeping its own name is fine.
        mockMvc.perform(put("/api/admin/technologies/" + kotlin).header(HttpHeaders.AUTHORIZATION, admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"java\", \"category\": \"BACKEND\", \"displayOrder\": 2}"))
                .andExpect(status().isConflict());
        mockMvc.perform(put("/api/admin/technologies/" + java).header(HttpHeaders.AUTHORIZATION, admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"JAVA\", \"category\": \"BACKEND\", \"displayOrder\": 1}"))
                .andExpect(status().isOk());

        // The V6 index enforces it even outside the API.
        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO technologies (name, category) VALUES ('kotlin', 'BACKEND')"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldKeepATechnologyThatAProjectUses() throws Exception {
        long java = id(save("Java", "BACKEND", 1));
        long project = jdbcTemplate.queryForObject("""
                INSERT INTO projects (name, slug, short_description, description)
                VALUES ('Sistema ERP', 'sistema-erp', 'Corta', 'Completa') RETURNING id
                """, Long.class);
        jdbcTemplate.update("INSERT INTO project_technologies VALUES (?, ?)", project, java);

        mockMvc.perform(get("/api/admin/technologies").header(HttpHeaders.AUTHORIZATION, admin()))
                .andExpect(jsonPath("$[0].projectCount").value(1));
        // RN005
        remove(java).andExpect(status().isConflict());

        jdbcTemplate.update("DELETE FROM project_technologies WHERE technology_id = ?", java);
        remove(java).andExpect(status().isNoContent());
    }

    private ResultActions save(String name, String category, int order) throws Exception {
        return mockMvc.perform(post("/api/admin/technologies").header(HttpHeaders.AUTHORIZATION, admin())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"%s\", \"category\": \"%s\", \"displayOrder\": %d}".formatted(name, category, order)));
    }

    private ResultActions remove(long id) throws Exception {
        return mockMvc.perform(delete("/api/admin/technologies/" + id).header(HttpHeaders.AUTHORIZATION, admin()));
    }

    private static long id(ResultActions result) throws Exception {
        return ((Number) JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.id")).longValue();
    }

    private String admin() {
        return "Bearer " + TestJwt.token(jwtEncoder, TestJwt.ISSUER, Instant.now().plusSeconds(60), "ADMIN");
    }
}
