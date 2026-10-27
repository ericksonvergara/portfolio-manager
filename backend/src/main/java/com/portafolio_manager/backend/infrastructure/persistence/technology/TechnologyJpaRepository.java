package com.portafolio_manager.backend.infrastructure.persistence.technology;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface TechnologyJpaRepository extends JpaRepository<TechnologyJpaEntity, Long> {

    List<TechnologyJpaEntity> findAllByOrderByDisplayOrderAscNameAsc();

    @Query("""
            SELECT COUNT(t) > 0 FROM TechnologyJpaEntity t
            WHERE LOWER(t.name) = LOWER(:name) AND (:excludedId IS NULL OR t.id <> :excludedId)
            """)
    boolean existsByNameIgnoringCase(@Param("name") String name, @Param("excludedId") Long excludedId);

    /** Pairs of technology id and number of projects that use it (only technologies in use). */
    @Query(
            value = "SELECT technology_id, COUNT(*) FROM project_technologies GROUP BY technology_id",
            nativeQuery = true
    )
    List<Object[]> countProjectsByTechnology();

    @Query(value = "SELECT COUNT(*) FROM project_technologies WHERE technology_id = :id", nativeQuery = true)
    long countProjects(@Param("id") Long id);
}
