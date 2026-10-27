package com.portafolio_manager.backend.application.service.profile;

import com.portafolio_manager.backend.application.port.in.profile.RemoveProfilePhotoUseCase;
import com.portafolio_manager.backend.application.port.out.image.ImageStoragePort;
import com.portafolio_manager.backend.application.port.out.profile.ProfileRepositoryPort;
import com.portafolio_manager.backend.domain.profile.Profile;
import com.portafolio_manager.backend.domain.profile.ProfileNotFoundException;

public class RemoveProfilePhotoService implements RemoveProfilePhotoUseCase {

    private final ProfileRepositoryPort profileRepository;
    private final ImageStoragePort imageStorage;

    public RemoveProfilePhotoService(ProfileRepositoryPort profileRepository, ImageStoragePort imageStorage) {
        this.profileRepository = profileRepository;
        this.imageStorage = imageStorage;
    }

    @Override
    public Profile execute(Void input) {
        Profile profile = profileRepository.find().orElseThrow(ProfileNotFoundException::new);
        if (profile.photoUrl() == null) {
            return profile;
        }

        Profile updated = profileRepository.setPhoto(null).orElseThrow(ProfileNotFoundException::new);
        imageStorage.delete(profile.photoUrl());
        return updated;
    }
}
