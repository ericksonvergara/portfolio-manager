package com.portafolio_manager.backend.infrastructure.web.profile;

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
 * The public landing endpoints against the real schema, without credentials.
 */
@SpringBootTest(properties = TestJwt.SECRET_PROPERTY)
@AutoConfigureMockMvc
class PublicContentIntegrationTest extends AbstractPostgresIntegrationTest {

    private static final String INSERT_PROFILE = """
            INSERT INTO profiles (full_name, headline, about, email)
            VALUES ('Erickson Vergara', 'Desarrollador Full Stack', 'Presentación', 'contacto@example.com')
            RETURNING id
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM profiles");
        jdbcTemplate.update("DELETE FROM technologies");
    }

    @Test
    void shouldAnswerNotFoundUntilTheProfileExists() throws Exception {
        mockMvc.perform(get("/api/public/profile")).andExpect(status().isNotFound());

        Long profileId = jdbcTemplate.queryForObject(INSERT_PROFILE, Long.class);
        jdbcTemplate.update("""
                INSERT INTO social_links (profile_id, platform, url, display_order)
                VALUES (?, 'LINKEDIN', 'https://www.linkedin.com/in/example', 2),
                       (?, 'GITHUB', 'https://github.com/ericksonvergara', 1)
                """, profileId, profileId);

        mockMvc.perform(get("/api/public/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Erickson Vergara"))
                .andExpect(jsonPath("$.photoUrl").doesNotExist())
                .andExpect(jsonPath("$.socialLinks", hasSize(2)))
                .andExpect(jsonPath("$.socialLinks[0].platform").value("GITHUB"))
                .andExpect(jsonPath("$.socialLinks[1].platform").value("LINKEDIN"));
    }

    @Test
    void shouldRejectASecondProfile() {
        jdbcTemplate.queryForObject(INSERT_PROFILE, Long.class);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(INSERT_PROFILE, Long.class))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldListTechnologiesByDisplayOrderThenName() throws Exception {
        mockMvc.perform(get("/api/public/technologies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        jdbcTemplate.update("""
                INSERT INTO technologies (name, category, display_order)
                VALUES ('React', 'FRONTEND', 2), ('Spring Boot', 'BACKEND', 1), ('Java', 'BACKEND', 1)
                """);

        mockMvc.perform(get("/api/public/technologies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name").value(contains("Java", "Spring Boot", "React")));
    }
}
