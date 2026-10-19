package com.portafolio_manager.backend.infrastructure.web.profile;

import com.portafolio_manager.backend.application.port.in.profile.GetPublicProfileUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/profile")
class PublicProfileController implements PublicProfileApi {

    private final GetPublicProfileUseCase getPublicProfileUseCase;

    PublicProfileController(GetPublicProfileUseCase getPublicProfileUseCase) {
        this.getPublicProfileUseCase = getPublicProfileUseCase;
    }

    @Override
    @GetMapping
    public ProfileResponse getProfile() {
        return ProfileResponse.from(getPublicProfileUseCase.execute(null));
    }
}
