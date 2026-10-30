package com.portafolio_manager.backend.infrastructure.persistence.project;

import com.portafolio_manager.backend.domain.project.DuplicateProjectNameException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Locale;
import java.util.Set;

/**
 * Tells apart the database rejections of a project save. Only the unique name and slug mean a duplicated
 * name (RN001); anything else, for example a technology deleted at the same time, is reported as it is.
 */
final class ProjectConstraints {

    /** The unique constraints of V4__create_project_schema.sql on projects.name and projects.slug. */
    private static final Set<String> UNIQUE_NAME = Set.of("projects_name_key", "projects_slug_key");

    private ProjectConstraints() {
    }

    static RuntimeException translate(DataIntegrityViolationException exception, String name) {
        return violatesUniqueName(exception) ? new DuplicateProjectNameException(name) : exception;
    }

    private static boolean violatesUniqueName(Throwable exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException violation && violation.getConstraintName() != null) {
                return UNIQUE_NAME.contains(violation.getConstraintName().toLowerCase(Locale.ROOT));
            }
        }
        return false;
    }
}
