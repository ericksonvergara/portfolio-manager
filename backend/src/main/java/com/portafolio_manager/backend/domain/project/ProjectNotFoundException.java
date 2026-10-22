package com.portafolio_manager.backend.domain.project;

/**
 * The project does not exist. On the public site an inactive project is reported the same way,
 * so the public API does not reveal that it exists.
 */
public class ProjectNotFoundException extends RuntimeException {

    private ProjectNotFoundException(String message) {
        super(message);
    }

    public static ProjectNotFoundException withSlug(String slug) {
        return new ProjectNotFoundException("No published project with slug '" + slug + "'");
    }

    public static ProjectNotFoundException withId(Long id) {
        return new ProjectNotFoundException("No project with id " + id);
    }
}
