package com.portafolio_manager.backend.application.service.profile;

import com.portafolio_manager.backend.application.port.in.profile.SetProfilePhotoUseCase;
import com.portafolio_manager.backend.application.port.out.image.ImageStoragePort;
import com.portafolio_manager.backend.application.port.out.profile.ProfileRepositoryPort;
import com.portafolio_manager.backend.application.service.image.StoredImages;
import com.portafolio_manager.backend.domain.image.ImageUpload;
import com.portafolio_manager.backend.domain.profile.Profile;
import com.portafolio_manager.backend.domain.profile.ProfileNotFoundException;

public class SetProfilePhotoService implements SetProfilePhotoUseCase {

    private final ProfileRepositoryPort profileRepository;
    private final ImageStoragePort imageStorage;
    private final long maxImageBytes;

    public SetProfilePhotoService(ProfileRepositoryPort profileRepository, ImageStoragePort imageStorage, long maxImageBytes) {
        this.profileRepository = profileRepository;
        this.imageStorage = imageStorage;
        this.maxImageBytes = maxImageBytes;
    }

    @Override
    public Profile execute(ImageUpload image) {
        Profile current = profileRepository.find().orElseThrow(ProfileNotFoundException::new);

        Profile updated = StoredImages.storeThen(image, maxImageBytes, imageStorage, url ->
                profileRepository.setPhoto(url).orElseThrow(ProfileNotFoundException::new));

        if (current.photoUrl() != null) {
            imageStorage.delete(current.photoUrl());
        }
        return updated;
    }
}
