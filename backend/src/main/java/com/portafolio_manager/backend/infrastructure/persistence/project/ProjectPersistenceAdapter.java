package com.portafolio_manager.backend.infrastructure.persistence.project;

import com.portafolio_manager.backend.application.port.out.project.ProjectRepositoryPort;
import com.portafolio_manager.backend.domain.project.ProjectDetail;
import com.portafolio_manager.backend.domain.project.ProjectImage;
import com.portafolio_manager.backend.domain.project.ProjectSummary;
import com.portafolio_manager.backend.domain.technology.Technology;
import com.portafolio_manager.backend.infrastructure.persistence.technology.TechnologyJpaEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
class ProjectPersistenceAdapter implements ProjectRepositoryPort {

    private final ProjectJpaRepository projectRepository;

    ProjectPersistenceAdapter(ProjectJpaRepository projectRepository) {
        this.projectRepository = projectRepository;
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
                .map(technology -> new Technology(technology.getName(), technology.getCategory()))
                .toList();
    }
}
