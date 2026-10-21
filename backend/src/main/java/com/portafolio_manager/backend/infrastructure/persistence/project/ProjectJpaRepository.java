package com.portafolio_manager.backend.infrastructure.persistence.project;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
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

    @EntityGraph(attributePaths = "technologies")
    List<ProjectJpaEntity> findAllByOrderByDisplayOrderAscNameAsc();

    boolean existsByNameIgnoreCaseOrSlug(String name, String slug);

    @Query("""
            SELECT COUNT(p) > 0 FROM ProjectJpaEntity p
            WHERE p.id <> :id AND (LOWER(p.name) = LOWER(:name) OR p.slug = :slug)
            """)
    boolean existsOtherByNameOrSlug(@Param("id") Long id, @Param("name") String name, @Param("slug") String slug);

    @EntityGraph(attributePaths = "technologies")
    Optional<ProjectJpaEntity> findWithTechnologiesById(Long id);
}
