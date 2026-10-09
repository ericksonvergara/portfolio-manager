package com.portafolio_manager.backend.infrastructure.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Set;

interface RoleJpaRepository extends JpaRepository<RoleJpaEntity, Long> {

    Set<RoleJpaEntity> findByNameIn(Collection<String> names);
}
