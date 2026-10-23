package com.portafolio_manager.backend.infrastructure.persistence.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

interface PasswordResetTokenJpaRepository extends JpaRepository<PasswordResetTokenJpaEntity, Long> {

    Optional<PasswordResetTokenJpaEntity> findByTokenHash(String tokenHash);

    @Modifying
    @Query("""
            UPDATE PasswordResetTokenJpaEntity t
            SET t.usedAt = :usedAt
            WHERE t.id = :id AND t.usedAt IS NULL
            """)
    int markUsedIfUnused(@Param("id") Long id, @Param("usedAt") Instant usedAt);

    @Modifying
    @Query("""
            UPDATE PasswordResetTokenJpaEntity t
            SET t.usedAt = :usedAt
            WHERE t.userId = :userId AND t.usedAt IS NULL
            """)
    void markAllUnusedAsUsed(@Param("userId") Long userId, @Param("usedAt") Instant usedAt);
}
