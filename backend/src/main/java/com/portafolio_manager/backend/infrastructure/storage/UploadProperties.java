package com.portafolio_manager.backend.infrastructure.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Where the uploaded images are written (UPLOADS_DIR) and their maximum size (UPLOAD_MAX_SIZE, RF011).
 */
@ConfigurationProperties(prefix = "app.uploads")
public record UploadProperties(Path dir, DataSize maxSize) {

    public UploadProperties {
        Objects.requireNonNull(dir, "app.uploads.dir must not be null");
        Objects.requireNonNull(maxSize, "app.uploads.max-size must not be null");
        if (maxSize.toBytes() <= 0) {
            throw new IllegalArgumentException("app.uploads.max-size (UPLOAD_MAX_SIZE) must be positive");
        }
    }
}
