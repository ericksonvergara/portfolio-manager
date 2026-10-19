package com.portafolio_manager.backend.infrastructure.persistence.project;

import com.portafolio_manager.backend.application.port.out.project.ProjectRepositoryPort;
import com.portafolio_manager.backend.domain.project.ProjectSummary;
import com.portafolio_manager.backend.domain.technology.Technology;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    private static ProjectSummary toSummary(ProjectJpaEntity entity) {
        return new ProjectSummary(
                entity.getSlug(),
                entity.getName(),
                entity.getShortDescription(),
                entity.getCoverImageUrl(),
                entity.getProjectUrl(),
                entity.getRepositoryUrl(),
                entity.isFeatured(),
                entity.getTechnologies().stream()
                        .map(technology -> new Technology(technology.getName(), technology.getCategory()))
                        .toList()
        );
    }
}
