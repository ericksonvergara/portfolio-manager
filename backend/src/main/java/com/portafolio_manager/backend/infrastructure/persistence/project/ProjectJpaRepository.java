package com.portafolio_manager.backend.infrastructure.persistence.project;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

interface ProjectJpaRepository extends JpaRepository<ProjectJpaEntity, Long> {

    @EntityGraph(attributePaths = "technologies")
    List<ProjectJpaEntity> findAllByActiveTrueOrderByFeaturedDescDisplayOrderAscNameAsc();

    /*
     * Only the technologies are fetched with the project: fetching both lists in one query is not
     * allowed (two bags). The gallery loads lazily inside the adapter's transaction.
     */
    @EntityGraph(attributePaths = "technologies")
    Optional<ProjectJpaEntity> findBySlugAndActiveTrue(String slug);

    @Query("SELECT p.id FROM ProjectJpaEntity p WHERE p.slug = :slug AND p.active = TRUE")
    Optional<Long> findPublishedIdBySlug(@Param("slug") String slug);

    @EntityGraph(attributePaths = "technologies")
    List<ProjectJpaEntity> findAllByOrderByDisplayOrderAscNameAsc();

    /** Pairs of project id and number of visits to its detail (only projects with visits, HU026). */
    @Query(
            value = "SELECT project_id, COUNT(*) FROM visits WHERE project_id IS NOT NULL GROUP BY project_id",
            nativeQuery = true
    )
    List<Object[]> countVisitsByProject();

    @Query(value = "SELECT COUNT(*) FROM visits WHERE project_id = :id", nativeQuery = true)
    long countVisits(@Param("id") Long id);

    boolean existsByNameIgnoreCaseOrSlug(String name, String slug);

    @Query("""
            SELECT COUNT(p) > 0 FROM ProjectJpaEntity p
            WHERE p.id <> :id AND (LOWER(p.name) = LOWER(:name) OR p.slug = :slug)
            """)
    boolean existsOtherByNameOrSlug(@Param("id") Long id, @Param("name") String name, @Param("slug") String slug);

    @EntityGraph(attributePaths = "technologies")
    Optional<ProjectJpaEntity> findWithTechnologiesById(Long id);

    /*
     * A direct DELETE: the schema removes the gallery and the technology links (ON DELETE CASCADE).
     * Deleting through the entity would first set the gallery's NOT NULL project_id to NULL.
     */
    @Modifying
    @Query("DELETE FROM ProjectJpaEntity p WHERE p.id = :id")
    int deleteProjectById(@Param("id") Long id);
}
