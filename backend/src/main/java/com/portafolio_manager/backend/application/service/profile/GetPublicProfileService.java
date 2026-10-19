package com.portafolio_manager.backend.application.service.profile;

import com.portafolio_manager.backend.application.port.in.profile.GetPublicProfileUseCase;
import com.portafolio_manager.backend.application.port.out.profile.ProfileRepositoryPort;
import com.portafolio_manager.backend.domain.profile.Profile;
import com.portafolio_manager.backend.domain.profile.ProfileNotFoundException;

public class GetPublicProfileService implements GetPublicProfileUseCase {

    private final ProfileRepositoryPort profileRepository;

    public GetPublicProfileService(ProfileRepositoryPort profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    public Profile execute(Void input) {
        return profileRepository.find().orElseThrow(ProfileNotFoundException::new);
    }
}
