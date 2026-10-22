package com.portafolio_manager.backend.domain.project;

import java.util.Objects;

/**
 * An image of the project gallery as the admin panel manages it (HU019).
 */
public record GalleryImage(Long id, String url, String altText) {

    public GalleryImage {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(url, "url must not be null");
    }
}
