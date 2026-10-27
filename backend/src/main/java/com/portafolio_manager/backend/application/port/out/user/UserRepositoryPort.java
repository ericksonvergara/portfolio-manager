package com.portafolio_manager.backend.application.port.out.user;

import com.portafolio_manager.backend.application.port.out.PersistencePort;
import com.portafolio_manager.backend.domain.user.User;

import java.time.Instant;
import java.util.Optional;

public interface UserRepositoryPort extends PersistencePort {

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    boolean existsByEmail(String email);

    User save(User user);

    void updateLastLogin(Long userId, Instant lastLoginAt);

    void updatePassword(Long userId, String passwordHash);
}
