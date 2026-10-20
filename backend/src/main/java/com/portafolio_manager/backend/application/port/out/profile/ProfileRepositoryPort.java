package com.portafolio_manager.backend.application.port.out.profile;

import com.portafolio_manager.backend.application.port.out.PersistencePort;
import com.portafolio_manager.backend.domain.profile.Profile;

import java.util.Optional;

public interface ProfileRepositoryPort extends PersistencePort {

    /** The single profile, with its social links in display order. */
    Optional<Profile> find();
}
