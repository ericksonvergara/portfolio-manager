package com.portafolio_manager.backend.application.port.in.profile;

import com.portafolio_manager.backend.application.port.in.UseCase;
import com.portafolio_manager.backend.domain.profile.Profile;
import com.portafolio_manager.backend.domain.profile.ProfileDraft;

/**
 * Saves the public profile (HU023), creating it the first time: production has no demo seed.
 */
public interface UpdateProfileUseCase extends UseCase<ProfileDraft, Profile> {
}
