package com.portafolio_manager.backend.infrastructure.config;

import com.portafolio_manager.backend.application.port.in.profile.GetPublicProfileUseCase;
import com.portafolio_manager.backend.application.port.in.project.CreateProjectUseCase;
import com.portafolio_manager.backend.application.port.in.project.DeleteProjectUseCase;
import com.portafolio_manager.backend.application.port.in.project.GetAdminProjectUseCase;
import com.portafolio_manager.backend.application.port.in.project.GetPublishedProjectUseCase;
import com.portafolio_manager.backend.application.port.in.project.ListAdminProjectsUseCase;
import com.portafolio_manager.backend.application.port.in.project.ListPublishedProjectsUseCase;
import com.portafolio_manager.backend.application.port.in.project.UpdateProjectUseCase;
import com.portafolio_manager.backend.application.port.in.technology.ListTechnologiesUseCase;
import com.portafolio_manager.backend.application.port.out.profile.ProfileRepositoryPort;
import com.portafolio_manager.backend.application.port.out.project.ProjectRepositoryPort;
import com.portafolio_manager.backend.application.port.out.technology.TechnologyRepositoryPort;
import com.portafolio_manager.backend.application.service.profile.GetPublicProfileService;
import com.portafolio_manager.backend.application.service.project.CreateProjectService;
import com.portafolio_manager.backend.application.service.project.DeleteProjectService;
import com.portafolio_manager.backend.application.service.project.GetAdminProjectService;
import com.portafolio_manager.backend.application.service.project.GetPublishedProjectService;
import com.portafolio_manager.backend.application.service.project.ListAdminProjectsService;
import com.portafolio_manager.backend.application.service.project.ListPublishedProjectsService;
import com.portafolio_manager.backend.application.service.project.UpdateProjectService;
import com.portafolio_manager.backend.application.service.technology.ListTechnologiesService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the framework-free services behind the portfolio content: the public site and its administration.
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

    @Bean
    ListPublishedProjectsUseCase listPublishedProjectsUseCase(ProjectRepositoryPort projectRepository) {
        return new ListPublishedProjectsService(projectRepository);
    }

    @Bean
    GetPublishedProjectUseCase getPublishedProjectUseCase(ProjectRepositoryPort projectRepository) {
        return new GetPublishedProjectService(projectRepository);
    }

    @Bean
    ListAdminProjectsUseCase listAdminProjectsUseCase(ProjectRepositoryPort projectRepository) {
        return new ListAdminProjectsService(projectRepository);
    }

    @Bean
    CreateProjectUseCase createProjectUseCase(
            ProjectRepositoryPort projectRepository,
            TechnologyRepositoryPort technologyRepository
    ) {
        return new CreateProjectService(projectRepository, technologyRepository);
    }

    @Bean
    GetAdminProjectUseCase getAdminProjectUseCase(ProjectRepositoryPort projectRepository) {
        return new GetAdminProjectService(projectRepository);
    }

    @Bean
    UpdateProjectUseCase updateProjectUseCase(
            ProjectRepositoryPort projectRepository,
            TechnologyRepositoryPort technologyRepository
    ) {
        return new UpdateProjectService(projectRepository, technologyRepository);
    }

    @Bean
    DeleteProjectUseCase deleteProjectUseCase(ProjectRepositoryPort projectRepository) {
        return new DeleteProjectService(projectRepository);
    }
}
