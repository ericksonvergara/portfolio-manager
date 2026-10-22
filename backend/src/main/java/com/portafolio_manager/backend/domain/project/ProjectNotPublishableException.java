package com.portafolio_manager.backend.domain.project;

/**
 * Only projects with a cover image can be published (RN004): a project without one cannot be
 * activated, and an active project cannot lose it.
 */
public class ProjectNotPublishableException extends RuntimeException {

    private ProjectNotPublishableException(String message) {
        super(message);
    }

    public static ProjectNotPublishableException withoutCover(String name) {
        return new ProjectNotPublishableException(
                "Project '" + name + "' needs a cover image before it can be published");
    }

    public static ProjectNotPublishableException activeKeepsCover(String name) {
        return new ProjectNotPublishableException(
                "Project '" + name + "' is published: deactivate it before removing its cover image");
    }
}
