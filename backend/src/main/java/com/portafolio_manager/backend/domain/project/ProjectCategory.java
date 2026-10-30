package com.portafolio_manager.backend.domain.project;

import com.portafolio_manager.backend.domain.technology.Technology;
import com.portafolio_manager.backend.domain.technology.TechnologyCategory;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * The kinds of project a visitor can filter by (HU005). A project's kind follows from its technologies, so
 * the administrator never classifies it twice: a Full Stack project uses both backend and frontend ones.
 */
public enum ProjectCategory {
    BACKEND(TechnologyCategory.BACKEND),
    FRONTEND(TechnologyCategory.FRONTEND),
    FULL_STACK(TechnologyCategory.BACKEND, TechnologyCategory.FRONTEND),
    MOBILE(TechnologyCategory.MOBILE),
    DEVOPS(TechnologyCategory.DEVOPS),
    AI(TechnologyCategory.AI);

    /** Every one of these must be among the categories of the project's technologies. */
    private final Set<TechnologyCategory> required;

    ProjectCategory(TechnologyCategory... required) {
        this.required = Set.of(required);
    }

    public boolean includes(ProjectSummary project) {
        Set<TechnologyCategory> used = project.technologies().stream()
                .map(Technology::category)
                .collect(Collectors.toSet());
        return used.containsAll(required);
    }
}
