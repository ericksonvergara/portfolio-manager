package com.portafolio_manager.backend.domain.project;

/**
 * No published project has that slug. An inactive project is reported the same way, so the
 * public API does not reveal that it exists.
 */
public class ProjectNotFoundException extends RuntimeException {

    public ProjectNotFoundException(String slug) {
        super("No published project with slug '" + slug + "'");
    }
}
