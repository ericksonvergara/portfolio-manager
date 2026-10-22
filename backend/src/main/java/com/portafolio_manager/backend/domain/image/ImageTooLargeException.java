package com.portafolio_manager.backend.domain.image;

/**
 * The uploaded image exceeds the configured maximum size (RF011).
 */
public class ImageTooLargeException extends InvalidImageException {

    public ImageTooLargeException(long maxBytes) {
        super("The image exceeds the maximum size of " + (maxBytes / (1024 * 1024)) + " MB");
    }
}
