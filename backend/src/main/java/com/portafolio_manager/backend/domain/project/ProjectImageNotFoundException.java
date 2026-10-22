package com.portafolio_manager.backend.domain.project;

/**
 * The gallery of the project has no image with that id.
 */
public class ProjectImageNotFoundException extends RuntimeException {

    public ProjectImageNotFoundException(Long projectId, Long imageId) {
        super("Project " + projectId + " has no image with id " + imageId);
    }
}
