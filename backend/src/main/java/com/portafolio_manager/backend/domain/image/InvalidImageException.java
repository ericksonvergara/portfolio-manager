package com.portafolio_manager.backend.domain.image;

/**
 * The uploaded file is not an accepted image: empty, wrong extension, wrong declared type, or a
 * content that is not JPG or PNG (RF011).
 */
public class InvalidImageException extends RuntimeException {

    public InvalidImageException(String message) {
        super(message);
    }
}
