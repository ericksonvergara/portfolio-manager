package com.portafolio_manager.backend.infrastructure.persistence.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

interface ProjectImageJpaRepository extends JpaRepository<ProjectImageJpaEntity, Long> {

    Optional<ProjectImageJpaEntity> findByIdAndProjectId(Long id, Long projectId);

    @Query("SELECT COALESCE(MAX(i.displayOrder), 0) FROM ProjectImageJpaEntity i WHERE i.projectId = :projectId")
    int findLastDisplayOrder(@Param("projectId") Long projectId);
}
