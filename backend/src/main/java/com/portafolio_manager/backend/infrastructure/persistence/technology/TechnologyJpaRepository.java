package com.portafolio_manager.backend.infrastructure.persistence.technology;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface TechnologyJpaRepository extends JpaRepository<TechnologyJpaEntity, Long> {

    List<TechnologyJpaEntity> findAllByOrderByDisplayOrderAscNameAsc();
}
