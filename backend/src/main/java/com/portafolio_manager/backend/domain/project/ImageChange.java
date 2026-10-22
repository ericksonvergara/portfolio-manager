package com.portafolio_manager.backend.domain.project;

import com.portafolio_manager.backend.domain.image.ImageUpload;

import java.util.Objects;

/**
 * A new cover image, or a new gallery image with its alternative text (HU019).
 */
public record ImageChange(Long projectId, ImageUpload image, String altText) {

    public ImageChange {
        Objects.requireNonNull(projectId, "projectId must not be null");
        Objects.requireNonNull(image, "image must not be null");
        altText = altText == null || altText.isBlank() ? null : altText.strip();
        if (altText != null && altText.length() > 200) {
            throw new InvalidProjectException("altText must be at most 200 characters");
        }
    }
}
