package com.portafolio_manager.backend.application.service.project;

import com.portafolio_manager.backend.application.port.out.technology.TechnologyRepositoryPort;
import com.portafolio_manager.backend.domain.project.InvalidProjectException;

import java.util.Set;
import java.util.TreeSet;

/**
 * Checks that every technology of a project exists in the catalog.
 */
final class ProjectTechnologies {

    private ProjectTechnologies() {
    }

    static void requireKnown(Set<Long> ids, TechnologyRepositoryPort technologyRepository) {
        Set<Long> unknown = new TreeSet<>(ids);
        unknown.removeAll(technologyRepository.findExistingIds(ids));
        if (!unknown.isEmpty()) {
            throw new InvalidProjectException("Unknown technologies: " + unknown);
        }
    }
}
