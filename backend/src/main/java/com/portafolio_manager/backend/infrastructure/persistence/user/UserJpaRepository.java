package com.portafolio_manager.backend.infrastructure.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {

    Optional<UserJpaEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE UserJpaEntity u SET u.lastLoginAt = :lastLoginAt WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId, @Param("lastLoginAt") Instant lastLoginAt);
}
