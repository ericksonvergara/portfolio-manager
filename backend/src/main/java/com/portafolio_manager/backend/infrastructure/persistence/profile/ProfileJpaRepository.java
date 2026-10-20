package com.portafolio_manager.backend.infrastructure.persistence.profile;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface ProfileJpaRepository extends JpaRepository<ProfileJpaEntity, Long> {

    @EntityGraph(attributePaths = "socialLinks")
    Optional<ProfileJpaEntity> findFirstByOrderByIdAsc();
}
