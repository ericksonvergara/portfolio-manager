package com.portafolio_manager.backend.infrastructure.persistence.project;

import com.portafolio_manager.backend.application.port.out.project.ProjectRepositoryPort;
import com.portafolio_manager.backend.domain.project.AdminProject;
import com.portafolio_manager.backend.domain.project.GalleryImage;
import com.portafolio_manager.backend.domain.project.ProjectDetail;
import com.portafolio_manager.backend.domain.project.ProjectDraft;
import com.portafolio_manager.backend.domain.project.ProjectNotPublishableException;
import com.portafolio_manager.backend.domain.project.ProjectImage;
import com.portafolio_manager.backend.domain.project.ProjectSummary;
import com.portafolio_manager.backend.domain.project.ProjectUpdate;
import com.portafolio_manager.backend.domain.technology.Technology;
import com.portafolio_manager.backend.infrastructure.persistence.technology.TechnologyJpaEntity;
import jakarta.persistence.EntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
class ProjectPersistenceAdapter implements ProjectRepositoryPort {

    private final ProjectJpaRepository projectRepository;
    private final ProjectImageJpaRepository imageRepository;
    private final EntityManager entityManager;

    ProjectPersistenceAdapter(
            ProjectJpaRepository projectRepository,
            ProjectImageJpaRepository imageRepository,
            EntityManager entityManager
    ) {
        this.projectRepository = projectRepository;
        this.imageRepository = imageRepository;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectSummary> findPublishedSummaries() {
        return projectRepository.findAllByActiveTrueOrderByFeaturedDescDisplayOrderAscNameAsc().stream()
                .map(ProjectPersistenceAdapter::toSummary)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProjectDetail> findPublishedBySlug(String slug) {
        return projectRepository.findBySlugAndActiveTrue(slug).map(ProjectPersistenceAdapter::toDetail);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminProject> findAllForAdmin() {
        return projectRepository.findAllByOrderByDisplayOrderAscNameAsc().stream()
                .map(ProjectPersistenceAdapter::toAdminProject)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNameOrSlug(String name, String slug) {
        return projectRepository.existsByNameIgnoreCaseOrSlug(name, slug);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsOtherByNameOrSlug(Long id, String name, String slug) {
        return projectRepository.existsOtherByNameOrSlug(id, name, slug);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AdminProject> findForAdmin(Long id) {
        return projectRepository.findWithTechnologiesById(id).map(ProjectPersistenceAdapter::toAdminProject);
    }

    @Override
    @Transactional
    public AdminProject create(ProjectDraft draft) {
        try {
            ProjectJpaEntity saved = projectRepository.saveAndFlush(
                    ProjectJpaEntity.newProject(draft, technologyReferences(draft)));
            return reloaded(saved);
        } catch (DataIntegrityViolationException exception) {
            // The name or slug was taken concurrently, after the service's check (RN001).
            throw ProjectConstraints.translate(exception, draft.name());
        }
    }

    @Override
    @Transactional
    public Optional<AdminProject> update(ProjectUpdate update) {
        Optional<ProjectJpaEntity> found = projectRepository.findById(update.id());
        if (found.isEmpty()) {
            return Optional.empty();
        }
        ProjectJpaEntity entity = found.get();
        entity.apply(update.draft(), technologyReferences(update.draft()));
        try {
            projectRepository.saveAndFlush(entity);
            return Optional.of(reloaded(entity));
        } catch (DataIntegrityViolationException exception) {
            throw ProjectConstraints.translate(exception, update.draft().name());
        }
    }

    @Override
    @Transactional
    public Optional<AdminProject> setActive(Long id, boolean active) {
        Optional<ProjectJpaEntity> found = projectRepository.findById(id);
        if (found.isEmpty()) {
            return Optional.empty();
        }
        ProjectJpaEntity entity = found.get();
        entity.setActive(active);
        try {
            // The schema also enforces RN004 (projects_published_with_cover).
            projectRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException exception) {
            throw ProjectNotPublishableException.withoutCover(entity.getName());
        }
        return Optional.of(reloaded(entity));
    }

    @Override
    @Transactional
    public Optional<AdminProject> setCover(Long id, String coverImageUrl) {
        Optional<ProjectJpaEntity> found = projectRepository.findById(id);
        if (found.isEmpty()) {
            return Optional.empty();
        }
        ProjectJpaEntity entity = found.get();
        entity.setCoverImageUrl(coverImageUrl);
        try {
            // The schema also enforces RN004 (projects_published_with_cover).
            projectRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException exception) {
            throw ProjectNotPublishableException.activeKeepsCover(entity.getName());
        }
        return Optional.of(reloaded(entity));
    }

    @Override
    @Transactional
    public Optional<AdminProject> addGalleryImage(Long projectId, String url, String altText) {
        if (!projectRepository.existsById(projectId)) {
            return Optional.empty();
        }
        int order = imageRepository.findLastDisplayOrder(projectId) + 1;
        imageRepository.saveAndFlush(ProjectImageJpaEntity.newImage(projectId, url, altText, order));
        return findForAdmin(projectId);
    }

    @Override
    @Transactional
    public Optional<String> removeGalleryImage(Long projectId, Long imageId) {
        Optional<ProjectImageJpaEntity> image = imageRepository.findByIdAndProjectId(imageId, projectId);
        image.ifPresent(imageRepository::delete);
        return image.map(ProjectImageJpaEntity::getImageUrl);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return projectRepository.deleteProjectById(id) > 0;
    }

    private List<TechnologyJpaEntity> technologyReferences(ProjectDraft draft) {
        return draft.technologyIds().stream()
                .map(id -> entityManager.getReference(TechnologyJpaEntity.class, id))
                .toList();
    }

    /** Reloads the project so its technologies come back complete and in display order. */
    private AdminProject reloaded(ProjectJpaEntity entity) {
        entityManager.refresh(entity);
        return toAdminProject(entity);
    }

    private static AdminProject toAdminProject(ProjectJpaEntity entity) {
        return new AdminProject(
                entity.getId(),
                entity.getSlug(),
                entity.getName(),
                entity.getShortDescription(),
                entity.getDescription(),
                entity.getCoverImageUrl(),
                entity.getProjectUrl(),
                entity.getRepositoryUrl(),
                entity.getVideoUrl(),
                entity.getStatus(),
                entity.getDevelopedOn(),
                entity.getDisplayOrder(),
                entity.isFeatured(),
                entity.isActive(),
                toTechnologies(entity.getTechnologies()),
                entity.getImages().stream()
                        .map(image -> new GalleryImage(image.getId(), image.getImageUrl(), image.getAltText()))
                        .toList()
        );
    }

    private static ProjectSummary toSummary(ProjectJpaEntity entity) {
        return new ProjectSummary(
                entity.getSlug(),
                entity.getName(),
                entity.getShortDescription(),
                entity.getCoverImageUrl(),
                entity.getProjectUrl(),
                entity.getRepositoryUrl(),
                entity.isFeatured(),
                toTechnologies(entity.getTechnologies())
        );
    }

    private static ProjectDetail toDetail(ProjectJpaEntity entity) {
        return new ProjectDetail(
                entity.getSlug(),
                entity.getName(),
                entity.getDescription(),
                entity.getCoverImageUrl(),
                entity.getProjectUrl(),
                entity.getRepositoryUrl(),
                entity.getVideoUrl(),
                entity.getStatus(),
                entity.getDevelopedOn(),
                entity.getImages().stream()
                        .map(image -> new ProjectImage(image.getImageUrl(), image.getAltText()))
                        .toList(),
                toTechnologies(entity.getTechnologies())
        );
    }

    private static List<Technology> toTechnologies(List<TechnologyJpaEntity> technologies) {
        return technologies.stream()
                .map(technology -> new Technology(technology.getId(), technology.getName(), technology.getCategory()))
                .toList();
    }
}
