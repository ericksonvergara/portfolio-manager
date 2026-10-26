package com.portafolio_manager.backend.infrastructure.config;

import com.portafolio_manager.backend.application.port.in.profile.GetPublicProfileUseCase;
import com.portafolio_manager.backend.application.port.in.profile.RemoveProfilePhotoUseCase;
import com.portafolio_manager.backend.application.port.in.profile.SetProfilePhotoUseCase;
import com.portafolio_manager.backend.application.port.in.profile.UpdateProfileUseCase;
import com.portafolio_manager.backend.application.port.in.project.AddGalleryImageUseCase;
import com.portafolio_manager.backend.application.port.in.project.CreateProjectUseCase;
import com.portafolio_manager.backend.application.port.in.project.DeleteProjectUseCase;
import com.portafolio_manager.backend.application.port.in.project.GetAdminProjectUseCase;
import com.portafolio_manager.backend.application.port.in.project.GetPublishedProjectUseCase;
import com.portafolio_manager.backend.application.port.in.project.ListAdminProjectsUseCase;
import com.portafolio_manager.backend.application.port.in.project.ListPublishedProjectsUseCase;
import com.portafolio_manager.backend.application.port.in.project.RemoveGalleryImageUseCase;
import com.portafolio_manager.backend.application.port.in.project.RemoveProjectCoverUseCase;
import com.portafolio_manager.backend.application.port.in.project.SetProjectCoverUseCase;
import com.portafolio_manager.backend.application.port.in.project.SetProjectPublicationUseCase;
import com.portafolio_manager.backend.application.port.in.project.UpdateProjectUseCase;
import com.portafolio_manager.backend.application.port.in.technology.CreateTechnologyUseCase;
import com.portafolio_manager.backend.application.port.in.technology.DeleteTechnologyUseCase;
import com.portafolio_manager.backend.application.port.in.technology.ListAdminTechnologiesUseCase;
import com.portafolio_manager.backend.application.port.in.technology.ListTechnologiesUseCase;
import com.portafolio_manager.backend.application.port.in.technology.UpdateTechnologyUseCase;
import com.portafolio_manager.backend.application.port.out.image.ImageStoragePort;
import com.portafolio_manager.backend.application.port.out.profile.ProfileRepositoryPort;
import com.portafolio_manager.backend.application.port.out.project.ProjectRepositoryPort;
import com.portafolio_manager.backend.application.port.out.technology.TechnologyRepositoryPort;
import com.portafolio_manager.backend.application.service.profile.GetPublicProfileService;
import com.portafolio_manager.backend.application.service.profile.RemoveProfilePhotoService;
import com.portafolio_manager.backend.application.service.profile.SetProfilePhotoService;
import com.portafolio_manager.backend.application.service.profile.UpdateProfileService;
import com.portafolio_manager.backend.application.service.project.AddGalleryImageService;
import com.portafolio_manager.backend.application.service.project.CreateProjectService;
import com.portafolio_manager.backend.application.service.project.DeleteProjectService;
import com.portafolio_manager.backend.application.service.project.GetAdminProjectService;
import com.portafolio_manager.backend.application.service.project.GetPublishedProjectService;
import com.portafolio_manager.backend.application.service.project.ListAdminProjectsService;
import com.portafolio_manager.backend.application.service.project.ListPublishedProjectsService;
import com.portafolio_manager.backend.application.service.project.RemoveGalleryImageService;
import com.portafolio_manager.backend.application.service.project.RemoveProjectCoverService;
import com.portafolio_manager.backend.application.service.project.SetProjectCoverService;
import com.portafolio_manager.backend.application.service.project.SetProjectPublicationService;
import com.portafolio_manager.backend.application.service.project.UpdateProjectService;
import com.portafolio_manager.backend.application.service.technology.CreateTechnologyService;
import com.portafolio_manager.backend.application.service.technology.DeleteTechnologyService;
import com.portafolio_manager.backend.application.service.technology.ListAdminTechnologiesService;
import com.portafolio_manager.backend.application.service.technology.ListTechnologiesService;
import com.portafolio_manager.backend.application.service.technology.UpdateTechnologyService;
import com.portafolio_manager.backend.infrastructure.storage.UploadProperties;
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
    UpdateProfileUseCase updateProfileUseCase(ProfileRepositoryPort profileRepository) {
        return new UpdateProfileService(profileRepository);
    }

    @Bean
    SetProfilePhotoUseCase setProfilePhotoUseCase(
            ProfileRepositoryPort profileRepository,
            ImageStoragePort imageStorage,
            UploadProperties uploads
    ) {
        return new SetProfilePhotoService(profileRepository, imageStorage, uploads.maxSize().toBytes());
    }

    @Bean
    RemoveProfilePhotoUseCase removeProfilePhotoUseCase(
            ProfileRepositoryPort profileRepository,
            ImageStoragePort imageStorage
    ) {
        return new RemoveProfilePhotoService(profileRepository, imageStorage);
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
    DeleteProjectUseCase deleteProjectUseCase(ProjectRepositoryPort projectRepository, ImageStoragePort imageStorage) {
        return new DeleteProjectService(projectRepository, imageStorage);
    }

    @Bean
    SetProjectCoverUseCase setProjectCoverUseCase(
            ProjectRepositoryPort projectRepository,
            ImageStoragePort imageStorage,
            UploadProperties uploads
    ) {
        return new SetProjectCoverService(projectRepository, imageStorage, uploads.maxSize().toBytes());
    }

    @Bean
    RemoveProjectCoverUseCase removeProjectCoverUseCase(
            ProjectRepositoryPort projectRepository,
            ImageStoragePort imageStorage
    ) {
        return new RemoveProjectCoverService(projectRepository, imageStorage);
    }

    @Bean
    AddGalleryImageUseCase addGalleryImageUseCase(
            ProjectRepositoryPort projectRepository,
            ImageStoragePort imageStorage,
            UploadProperties uploads
    ) {
        return new AddGalleryImageService(projectRepository, imageStorage, uploads.maxSize().toBytes());
    }

    @Bean
    RemoveGalleryImageUseCase removeGalleryImageUseCase(
            ProjectRepositoryPort projectRepository,
            ImageStoragePort imageStorage
    ) {
        return new RemoveGalleryImageService(projectRepository, imageStorage);
    }

    @Bean
    SetProjectPublicationUseCase setProjectPublicationUseCase(ProjectRepositoryPort projectRepository) {
        return new SetProjectPublicationService(projectRepository);
    }

    @Bean
    ListAdminTechnologiesUseCase listAdminTechnologiesUseCase(TechnologyRepositoryPort technologyRepository) {
        return new ListAdminTechnologiesService(technologyRepository);
    }

    @Bean
    CreateTechnologyUseCase createTechnologyUseCase(TechnologyRepositoryPort technologyRepository) {
        return new CreateTechnologyService(technologyRepository);
    }

    @Bean
    UpdateTechnologyUseCase updateTechnologyUseCase(TechnologyRepositoryPort technologyRepository) {
        return new UpdateTechnologyService(technologyRepository);
    }

    @Bean
    DeleteTechnologyUseCase deleteTechnologyUseCase(TechnologyRepositoryPort technologyRepository) {
        return new DeleteTechnologyService(technologyRepository);
    }
}
