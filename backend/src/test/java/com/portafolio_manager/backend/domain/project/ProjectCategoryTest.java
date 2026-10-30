package com.portafolio_manager.backend.domain.project;

import com.portafolio_manager.backend.domain.technology.Technology;
import com.portafolio_manager.backend.domain.technology.TechnologyCategory;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectCategoryTest {

    private static final ProjectSummary API = project(TechnologyCategory.BACKEND, TechnologyCategory.DATABASE);
    private static final ProjectSummary WEB = project(TechnologyCategory.BACKEND, TechnologyCategory.FRONTEND);
    private static final ProjectSummary APP = project(TechnologyCategory.MOBILE, TechnologyCategory.AI);
    private static final ProjectSummary NONE = project();

    @Test
    void shouldFollowTheCategoriesOfTheTechnologies() {
        assertThat(ProjectCategory.BACKEND.includes(API)).isTrue();
        assertThat(ProjectCategory.BACKEND.includes(WEB)).isTrue();
        assertThat(ProjectCategory.FRONTEND.includes(API)).isFalse();
        assertThat(ProjectCategory.MOBILE.includes(APP)).isTrue();
        assertThat(ProjectCategory.AI.includes(APP)).isTrue();
        assertThat(ProjectCategory.DEVOPS.includes(APP)).isFalse();
    }

    @Test
    void shouldCallFullStackAProjectWithBackendAndFrontendTechnologies() {
        assertThat(ProjectCategory.FULL_STACK.includes(WEB)).isTrue();
        assertThat(ProjectCategory.FULL_STACK.includes(API)).isFalse();
        assertThat(ProjectCategory.FULL_STACK.includes(APP)).isFalse();
    }

    @Test
    void shouldLeaveOutAProjectWithoutTechnologies() {
        assertThat(Arrays.stream(ProjectCategory.values()).noneMatch(category -> category.includes(NONE))).isTrue();
    }

    private static ProjectSummary project(TechnologyCategory... categories) {
        List<Technology> technologies = Arrays.stream(categories)
                .map(category -> new Technology((long) category.ordinal(), category.name(), category))
                .toList();
        return new ProjectSummary("demo", "Demo", "Corta.", "/images/demo.png", null, null, false, technologies);
    }
}
