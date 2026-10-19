package com.portafolio_manager.backend.infrastructure.persistence;

import com.portafolio_manager.backend.infrastructure.security.jwt.TestJwt;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyIterable;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Applies the local demo seed on top of the migrations, as docker compose does in development.
 * It uses its own database: once the seed is applied, Flyway rejects any context that does not
 * list the seed location, which is the case for the shared container of the other tests.
 */
@SpringBootTest(properties = {
        TestJwt.SECRET_PROPERTY,
        "spring.flyway.locations=classpath:db/migration,classpath:db/seed"
})
@AutoConfigureMockMvc
class LandingSeedIntegrationTest {

    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldServeTheDemoProfileAndTechnologies() throws Exception {
        mockMvc.perform(get("/api/public/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Erickson Vergara"))
                .andExpect(jsonPath("$.socialLinks", not(emptyIterable())));

        mockMvc.perform(get("/api/public/technologies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Java"));
    }

    @Test
    void shouldPublishTheActiveDemoProjectsOnly() throws Exception {
        mockMvc.perform(get("/api/public/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].slug").value(contains(
                        "portfolio-manager", "sistema-de-inventario", "agenda-de-citas")))
                .andExpect(jsonPath("$[0].technologies", not(emptyIterable())));
    }
}
