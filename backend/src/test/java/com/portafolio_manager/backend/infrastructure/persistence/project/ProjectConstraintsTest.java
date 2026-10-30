package com.portafolio_manager.backend.infrastructure.persistence.project;

import com.portafolio_manager.backend.domain.project.DuplicateProjectNameException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectConstraintsTest {

    @ParameterizedTest
    @ValueSource(strings = {"projects_name_key", "projects_slug_key", "PROJECTS_SLUG_KEY"})
    void shouldReportTheUniqueNameAndSlugAsADuplicatedName(String constraint) {
        RuntimeException translated = ProjectConstraints.translate(violation(constraint), "Sistema ERP");

        assertThat(translated).isInstanceOf(DuplicateProjectNameException.class).hasMessageContaining("Sistema ERP");
    }

    @Test
    void shouldLeaveAnyOtherRejectionAsItIs() {
        // For example a technology deleted while the project was being saved (HU022): not a duplicated name.
        DataIntegrityViolationException foreignKey = violation("project_technologies_technology_id_fkey");
        DataIntegrityViolationException unknown = new DataIntegrityViolationException("no details");

        assertThat(ProjectConstraints.translate(foreignKey, "Sistema ERP")).isSameAs(foreignKey);
        assertThat(ProjectConstraints.translate(unknown, "Sistema ERP")).isSameAs(unknown);
    }

    private static DataIntegrityViolationException violation(String constraint) {
        return new DataIntegrityViolationException("could not execute statement",
                new ConstraintViolationException("violates constraint", new SQLException(), constraint));
    }
}
