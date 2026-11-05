package com.portafolio_manager.backend.infrastructure.persistence.dashboard;

import com.portafolio_manager.backend.application.port.out.dashboard.DashboardRepositoryPort;
import com.portafolio_manager.backend.domain.dashboard.LatestProject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

/**
 * Read-only indicators, straight from the tables: they cross several aggregates, and no entity is needed
 * to count rows.
 */
@Component
class DashboardPersistenceAdapter implements DashboardRepositoryPort {

    private final JdbcTemplate jdbcTemplate;

    DashboardPersistenceAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional(readOnly = true)
    public long countProjects() {
        return count("SELECT COUNT(*) FROM projects");
    }

    @Override
    @Transactional(readOnly = true)
    public long countTechnologies() {
        return count("SELECT COUNT(*) FROM technologies");
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LatestProject> findLatestProject() {
        return jdbcTemplate.query(
                "SELECT name, created_at FROM projects ORDER BY created_at DESC, id DESC LIMIT 1",
                (row, number) -> new LatestProject(row.getString("name"), row.getTimestamp("created_at").toInstant())
        ).stream().findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Instant> findPreviousAccess(Long userId) {
        return jdbcTemplate.query(
                "SELECT previous_login_at FROM users WHERE id = ?",
                (row, number) -> Optional.ofNullable(row.getTimestamp("previous_login_at")).map(Timestamp::toInstant),
                userId
        ).stream().findFirst().flatMap(access -> access);
    }

    private long count(String sql) {
        Long total = jdbcTemplate.queryForObject(sql, Long.class);
        return total == null ? 0 : total;
    }
}
