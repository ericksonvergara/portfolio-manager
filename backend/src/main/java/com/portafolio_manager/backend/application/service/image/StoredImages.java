package com.portafolio_manager.backend.application.service.image;

import com.portafolio_manager.backend.application.port.out.image.ImageStoragePort;
import com.portafolio_manager.backend.domain.image.ImageUpload;

import java.util.function.Function;

/**
 * Validates and stores an uploaded image, then records it. If recording fails, the new file is
 * deleted so no orphan stays in the storage.
 */
public final class StoredImages {

    private StoredImages() {
    }

    public static <T> T storeThen(ImageUpload upload, long maxBytes, ImageStoragePort storage, Function<String, T> record) {
        String url = storage.store(upload.content(), upload.validate(maxBytes));
        try {
            return record.apply(url);
        } catch (RuntimeException exception) {
            storage.delete(url);
            throw exception;
        }
    }
}
