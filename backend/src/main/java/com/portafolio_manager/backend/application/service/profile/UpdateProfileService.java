package com.portafolio_manager.backend.application.service.profile;

import com.portafolio_manager.backend.application.port.in.profile.UpdateProfileUseCase;
import com.portafolio_manager.backend.application.port.out.profile.ProfileRepositoryPort;
import com.portafolio_manager.backend.domain.profile.Profile;
import com.portafolio_manager.backend.domain.profile.ProfileDraft;

public class UpdateProfileService implements UpdateProfileUseCase {

    private final ProfileRepositoryPort profileRepository;

    public UpdateProfileService(ProfileRepositoryPort profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    public Profile execute(ProfileDraft draft) {
        return profileRepository.save(draft);
    }
}
