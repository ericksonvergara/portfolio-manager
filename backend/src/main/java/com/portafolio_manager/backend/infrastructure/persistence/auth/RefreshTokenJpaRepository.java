package com.portafolio_manager.backend.infrastructure.persistence.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenJpaEntity, Long> {

    Optional<RefreshTokenJpaEntity> findByTokenHash(String tokenHash);

    @Modifying
    @Query("""
            UPDATE RefreshTokenJpaEntity t
            SET t.revokedAt = :revokedAt
            WHERE t.id = :id AND t.revokedAt IS NULL
            """)
    int revokeIfActive(@Param("id") Long id, @Param("revokedAt") Instant revokedAt);

    @Modifying
    @Query("""
            UPDATE RefreshTokenJpaEntity t
            SET t.revokedAt = :revokedAt
            WHERE t.userId = :userId AND t.revokedAt IS NULL
            """)
    void revokeAllActiveByUserId(@Param("userId") Long userId, @Param("revokedAt") Instant revokedAt);
}
