package com.portafolio_manager.backend.domain.profile;

public class ProfileNotFoundException extends RuntimeException {

    public ProfileNotFoundException() {
        super("The profile has not been set up yet");
    }
}
