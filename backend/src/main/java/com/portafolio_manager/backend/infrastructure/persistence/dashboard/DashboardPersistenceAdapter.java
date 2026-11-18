package com.portafolio_manager.backend.infrastructure.persistence.dashboard;

import com.portafolio_manager.backend.application.port.out.dashboard.DashboardRepositoryPort;
import com.portafolio_manager.backend.domain.dashboard.LatestProject;
import com.portafolio_manager.backend.domain.dashboard.LatestPublishedProject;
import com.portafolio_manager.backend.domain.dashboard.RankedProject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
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
    public Optional<LatestPublishedProject> findLatestPublishedProject() {
        return jdbcTemplate.query("""
                SELECT name, COALESCE(published_at, updated_at) AS published_at
                FROM projects
                WHERE active
                ORDER BY COALESCE(published_at, updated_at) DESC, id DESC
                LIMIT 1
                """,
                (row, number) -> new LatestPublishedProject(
                        row.getString("name"), row.getTimestamp("published_at").toInstant())
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

    @Override
    @Transactional(readOnly = true)
    public long countVisits() {
        return count("SELECT COUNT(*) FROM visits");
    }

    @Override
    @Transactional(readOnly = true)
    public long countVisitsSince(Instant start) {
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM visits WHERE visited_at >= ?", Long.class, Timestamp.from(start));
        return total == null ? 0 : total;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RankedProject> findMostVisitedProjects(int limit) {
        return jdbcTemplate.query("""
                SELECT p.id, p.name, p.active, COUNT(*) AS visits
                FROM visits v
                JOIN projects p ON p.id = v.project_id
                GROUP BY p.id, p.name, p.active
                ORDER BY visits DESC, p.name
                LIMIT ?
                """,
                (row, number) -> new RankedProject(
                        row.getLong("id"), row.getString("name"), row.getLong("visits"), row.getBoolean("active")),
                limit
        );
    }

    private long count(String sql) {
        Long total = jdbcTemplate.queryForObject(sql, Long.class);
        return total == null ? 0 : total;
    }
}
