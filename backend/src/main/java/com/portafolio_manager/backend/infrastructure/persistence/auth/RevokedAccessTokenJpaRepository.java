package com.portafolio_manager.backend.infrastructure.persistence.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

interface RevokedAccessTokenJpaRepository extends JpaRepository<RevokedAccessTokenJpaEntity, String> {

    @Modifying
    @Query(value = """
            INSERT INTO revoked_access_tokens (token_id, expires_at)
            VALUES (:tokenId, :expiresAt)
            ON CONFLICT (token_id) DO NOTHING
            """, nativeQuery = true)
    void insertIfAbsent(@Param("tokenId") String tokenId, @Param("expiresAt") Instant expiresAt);

    @Modifying
    @Query("DELETE FROM RevokedAccessTokenJpaEntity t WHERE t.expiresAt <= :now")
    void deleteExpired(@Param("now") Instant now);
}
