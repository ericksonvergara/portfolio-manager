package com.portafolio_manager.backend.infrastructure.persistence.project;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface ProjectJpaRepository extends JpaRepository<ProjectJpaEntity, Long> {

    @EntityGraph(attributePaths = "technologies")
    List<ProjectJpaEntity> findAllByActiveTrueOrderByFeaturedDescDisplayOrderAscNameAsc();
}
