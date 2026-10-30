package com.portafolio_manager.backend.infrastructure.persistence.contact;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface ContactMessageJpaRepository extends JpaRepository<ContactMessageJpaEntity, Long> {

    List<ContactMessageJpaEntity> findAllByOrderByReceivedAtDescIdDesc();
}
