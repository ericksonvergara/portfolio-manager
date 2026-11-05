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

    /**
     * Native because previous_login_at is not mapped: the entity is rebuilt from the domain on every save,
     * which would wipe a column that the domain does not carry.
     */
    @Modifying
    @Query(
            value = "UPDATE users SET previous_login_at = last_login_at, last_login_at = :lastLoginAt WHERE id = :userId",
            nativeQuery = true
    )
    void updateLastLogin(@Param("userId") Long userId, @Param("lastLoginAt") Instant lastLoginAt);

    @Modifying
    @Query("UPDATE UserJpaEntity u SET u.passwordHash = :passwordHash, u.updatedAt = :updatedAt WHERE u.id = :userId")
    void updatePassword(
            @Param("userId") Long userId,
            @Param("passwordHash") String passwordHash,
            @Param("updatedAt") Instant updatedAt
    );
}
