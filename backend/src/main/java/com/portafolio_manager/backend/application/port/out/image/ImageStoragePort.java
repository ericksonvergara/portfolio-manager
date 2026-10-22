package com.portafolio_manager.backend.application.port.out.image;

import com.portafolio_manager.backend.domain.image.ImageFormat;

/**
 * Where the uploaded images live. Each file gets a unique name (RN007) and a public URL.
 */
public interface ImageStoragePort {

    /** Stores the image under a new unique name and returns its public URL. */
    String store(byte[] content, ImageFormat format);

    /**
     * Deletes the file behind a URL returned by {@link #store}. URLs of other origins (for example
     * the demo images of the local seed) are ignored, and so is a file that no longer exists.
     */
    void delete(String url);
}
