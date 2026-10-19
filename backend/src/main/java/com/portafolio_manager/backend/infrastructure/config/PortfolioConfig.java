package com.portafolio_manager.backend.infrastructure.config;

import com.portafolio_manager.backend.application.port.in.profile.GetPublicProfileUseCase;
import com.portafolio_manager.backend.application.port.in.technology.ListTechnologiesUseCase;
import com.portafolio_manager.backend.application.port.out.profile.ProfileRepositoryPort;
import com.portafolio_manager.backend.application.port.out.technology.TechnologyRepositoryPort;
import com.portafolio_manager.backend.application.service.profile.GetPublicProfileService;
import com.portafolio_manager.backend.application.service.technology.ListTechnologiesService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the framework-free services behind the public portfolio content.
 */
@Configuration
public class PortfolioConfig {

    @Bean
    GetPublicProfileUseCase getPublicProfileUseCase(ProfileRepositoryPort profileRepository) {
        return new GetPublicProfileService(profileRepository);
    }

    @Bean
    ListTechnologiesUseCase listTechnologiesUseCase(TechnologyRepositoryPort technologyRepository) {
        return new ListTechnologiesService(technologyRepository);
    }
}
