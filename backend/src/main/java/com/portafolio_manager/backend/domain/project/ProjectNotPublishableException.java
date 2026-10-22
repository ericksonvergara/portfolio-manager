package com.portafolio_manager.backend.domain.project;

/**
 * Only projects with a cover image can be published (RN004).
 */
public class ProjectNotPublishableException extends RuntimeException {

    public ProjectNotPublishableException(String name) {
        super("Project '" + name + "' needs a cover image before it can be published");
    }
}
