package com.portafolio_manager.backend.domain.project;

import java.util.Objects;

/** An image of the project gallery; the alternative text is optional. */
public record ProjectImage(String url, String altText) {

    public ProjectImage {
        Objects.requireNonNull(url, "url must not be null");
    }
}
