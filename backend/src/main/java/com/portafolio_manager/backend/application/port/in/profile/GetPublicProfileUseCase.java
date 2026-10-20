package com.portafolio_manager.backend.application.port.in.profile;

import com.portafolio_manager.backend.application.port.in.UseCase;
import com.portafolio_manager.backend.domain.profile.Profile;

/**
 * Takes no input; it throws {@code ProfileNotFoundException} while the profile is not set up.
 */
public interface GetPublicProfileUseCase extends UseCase<Void, Profile> {
}
