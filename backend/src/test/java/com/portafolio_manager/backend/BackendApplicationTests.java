package com.portafolio_manager.backend;

import com.portafolio_manager.backend.infrastructure.AbstractPostgresIntegrationTest;
import com.portafolio_manager.backend.infrastructure.security.jwt.TestJwt;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = TestJwt.SECRET_PROPERTY)
class BackendApplicationTests extends AbstractPostgresIntegrationTest {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void contextLoads() {
	}

	@Test
	void shouldApplyFlywayMigration() {
		Integer tables = jdbcTemplate.queryForObject(
				"""
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = 'public'
                  AND table_name = 'flyway_schema_test'
                """,
				Integer.class
		);

		System.out.println("flyway_schema_test tables found: " + tables);
		assertThat(tables).isEqualTo(1);
	}
}
