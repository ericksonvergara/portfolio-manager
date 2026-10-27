package com.portafolio_manager.backend.application.port.out.profile;

import com.portafolio_manager.backend.application.port.out.PersistencePort;
import com.portafolio_manager.backend.domain.profile.Profile;
import com.portafolio_manager.backend.domain.profile.ProfileDraft;

import java.util.Optional;

public interface ProfileRepositoryPort extends PersistencePort {

    /** The single profile, with its social links in display order. */
    Optional<Profile> find();

    /** Creates the profile if there is none yet, or replaces its data; the photo is kept. */
    Profile save(ProfileDraft draft);

    /** Sets or clears ({@code null}) the photo URL; empty while the profile is not set up. */
    Optional<Profile> setPhoto(String photoUrl);
}
