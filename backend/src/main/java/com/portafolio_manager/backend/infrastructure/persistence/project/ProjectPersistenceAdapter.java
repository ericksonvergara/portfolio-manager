package com.portafolio_manager.backend.infrastructure.persistence.project;

import com.portafolio_manager.backend.application.port.out.project.ProjectRepositoryPort;
import com.portafolio_manager.backend.domain.project.AdminProject;
import com.portafolio_manager.backend.domain.project.DuplicateProjectNameException;
import com.portafolio_manager.backend.domain.project.ProjectDetail;
import com.portafolio_manager.backend.domain.project.ProjectDraft;
import com.portafolio_manager.backend.domain.project.ProjectImage;
import com.portafolio_manager.backend.domain.project.ProjectSummary;
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
    private final EntityManager entityManager;

    ProjectPersistenceAdapter(ProjectJpaRepository projectRepository, EntityManager entityManager) {
        this.projectRepository = projectRepository;
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
    @Transactional
    public AdminProject create(ProjectDraft draft) {
        List<TechnologyJpaEntity> technologies = draft.technologyIds().stream()
                .map(id -> entityManager.getReference(TechnologyJpaEntity.class, id))
                .toList();
        try {
            ProjectJpaEntity saved = projectRepository.saveAndFlush(ProjectJpaEntity.newProject(draft, technologies));
            // Reload so the technologies come back complete and in display order.
            entityManager.refresh(saved);
            return toAdminProject(saved);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateProjectNameException(draft.name());
        }
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
                toTechnologies(entity.getTechnologies())
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
