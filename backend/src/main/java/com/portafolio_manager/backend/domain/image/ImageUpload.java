package com.portafolio_manager.backend.domain.image;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * An image file sent by the administrator (HU019), before it is stored.
 */
public record ImageUpload(byte[] content, String originalFilename, String contentType) {

    private static final Map<String, Set<ImageFormat>> EXTENSIONS = Map.of(
            "jpg", Set.of(ImageFormat.JPEG),
            "jpeg", Set.of(ImageFormat.JPEG),
            "png", Set.of(ImageFormat.PNG)
    );

    public ImageUpload {
        Objects.requireNonNull(content, "content must not be null");
    }

    /**
     * Checks the size, the extension, the declared type and the real content (RF011), and returns
     * the format of the image.
     */
    public ImageFormat validate(long maxBytes) {
        if (content.length == 0) {
            throw new InvalidImageException("The file is empty");
        }
        if (content.length > maxBytes) {
            throw new ImageTooLargeException(maxBytes);
        }

        String name = originalFilename == null ? "" : originalFilename.toLowerCase(Locale.ROOT);
        String extension = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : "";
        Set<ImageFormat> allowed = EXTENSIONS.get(extension);
        if (allowed == null) {
            throw new InvalidImageException("Only JPG, JPEG and PNG files are allowed");
        }

        ImageFormat format = ImageFormat.detect(content)
                .orElseThrow(() -> new InvalidImageException("The file content is not a JPG or PNG image"));
        String declared = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
        if (!allowed.contains(format) || !format.mimeType().equals(declared)) {
            throw new InvalidImageException("The file type does not match its content");
        }
        return format;
    }
}
