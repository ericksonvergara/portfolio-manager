package com.portafolio_manager.backend.infrastructure;

import com.portafolio_manager.backend.infrastructure.storage.TestImages;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Starts a single PostgreSQL container shared by every integration test in the JVM,
 * instead of one container per test class. Testcontainers' Ryuk removes it when the JVM exits.
 */
public abstract class AbstractPostgresIntegrationTest {

    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("app.uploads.dir", TestImages.UPLOADS_DIR::toString);
    }
}
