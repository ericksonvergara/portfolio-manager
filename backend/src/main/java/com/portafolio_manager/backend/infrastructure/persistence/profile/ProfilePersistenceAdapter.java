package com.portafolio_manager.backend.infrastructure.persistence.profile;

import com.portafolio_manager.backend.application.port.out.profile.ProfileRepositoryPort;
import com.portafolio_manager.backend.domain.profile.Profile;
import com.portafolio_manager.backend.domain.profile.SocialLink;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
class ProfilePersistenceAdapter implements ProfileRepositoryPort {

    private final ProfileJpaRepository profileRepository;

    ProfilePersistenceAdapter(ProfileJpaRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Profile> find() {
        return profileRepository.findFirstByOrderByIdAsc().map(ProfilePersistenceAdapter::toDomain);
    }

    private static Profile toDomain(ProfileJpaEntity entity) {
        return new Profile(
                entity.getFullName(),
                entity.getHeadline(),
                entity.getAbout(),
                entity.getPhotoUrl(),
                entity.getEmail(),
                entity.getLocation(),
                entity.getSocialLinks().stream()
                        .map(link -> new SocialLink(link.getPlatform(), link.getUrl()))
                        .toList()
        );
    }
}
