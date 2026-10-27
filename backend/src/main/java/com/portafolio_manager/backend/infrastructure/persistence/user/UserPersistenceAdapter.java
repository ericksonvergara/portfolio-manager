package com.portafolio_manager.backend.infrastructure.persistence.user;

import com.portafolio_manager.backend.application.port.out.user.UserRepositoryPort;
import com.portafolio_manager.backend.domain.user.Role;
import com.portafolio_manager.backend.domain.user.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
class UserPersistenceAdapter implements UserRepositoryPort {

    private final UserJpaRepository userRepository;
    private final RoleJpaRepository roleRepository;

    UserPersistenceAdapter(UserJpaRepository userRepository, RoleJpaRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email).map(UserPersistenceAdapter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id).map(UserPersistenceAdapter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public User save(User user) {
        Set<String> roleNames = user.roles().stream().map(Role::name).collect(Collectors.toSet());
        Set<RoleJpaEntity> roles = roleRepository.findByNameIn(roleNames);

        if (roles.size() != roleNames.size()) {
            throw new IllegalStateException("Some roles do not exist: " + roleNames);
        }

        UserJpaEntity entity = new UserJpaEntity(
                user.id(),
                user.name(),
                user.email(),
                user.passwordHash(),
                user.active(),
                user.lastLoginAt(),
                roles
        );

        return toDomain(userRepository.save(entity));
    }

    @Override
    @Transactional
    public void updateLastLogin(Long userId, Instant lastLoginAt) {
        userRepository.updateLastLogin(userId, lastLoginAt);
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, String passwordHash) {
        userRepository.updatePassword(userId, passwordHash, Instant.now());
    }

    private static User toDomain(UserJpaEntity entity) {
        Set<Role> roles = entity.getRoles().stream()
                .map(role -> Role.valueOf(role.getName()))
                .collect(Collectors.toSet());

        return new User(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPasswordHash(),
                roles,
                entity.isActive(),
                entity.getLastLoginAt()
        );
    }
}
