package com.portafolio_manager.backend.domain.project;

import java.util.Objects;

/**
 * Identifies one image of a project gallery.
 */
public record GalleryImageRef(Long projectId, Long imageId) {

    public GalleryImageRef {
        Objects.requireNonNull(projectId, "projectId must not be null");
        Objects.requireNonNull(imageId, "imageId must not be null");
    }
}
