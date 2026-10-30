package com.portafolio_manager.backend.domain.project;

/**
 * Only complete projects can be published (HU030): they need a cover image (RN004) and at least one
 * technology (RN005). A project without them cannot be activated, and an active project cannot lose
 * its cover.
 */
public class ProjectNotPublishableException extends RuntimeException {

    private ProjectNotPublishableException(String message) {
        super(message);
    }

    public static ProjectNotPublishableException withoutCover(String name) {
        return missing(name, true, false);
    }

    /** Names everything the project lacks, so the administrator fixes it in one go. */
    public static ProjectNotPublishableException missing(String name, boolean cover, boolean technologies) {
        String what = cover && technologies ? "a cover image and at least one technology"
                : cover ? "a cover image"
                : "at least one technology";
        return new ProjectNotPublishableException(
                "Project '" + name + "' needs " + what + " before it can be published");
    }

    public static ProjectNotPublishableException activeKeepsCover(String name) {
        return new ProjectNotPublishableException(
                "Project '" + name + "' is published: deactivate it before removing its cover image");
    }
}
