package com.portafolio_manager.backend.infrastructure.persistence.visit;

import org.springframework.data.jpa.repository.JpaRepository;

interface VisitJpaRepository extends JpaRepository<VisitJpaEntity, Long> {
}
