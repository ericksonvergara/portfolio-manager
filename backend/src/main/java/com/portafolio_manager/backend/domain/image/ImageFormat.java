package com.portafolio_manager.backend.domain.image;

import java.util.Arrays;
import java.util.Optional;

/**
 * The image formats the portfolio accepts (RF011): JPG/JPEG and PNG. The format is recognised from
 * the first bytes of the file, not from its name or its declared type.
 */
public enum ImageFormat {

    JPEG("jpg", "image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}),
    PNG("png", "image/png", new byte[]{(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'});

    private final String extension;
    private final String mimeType;
    private final byte[] signature;

    ImageFormat(String extension, String mimeType, byte[] signature) {
        this.extension = extension;
        this.mimeType = mimeType;
        this.signature = signature;
    }

    public String extension() {
        return extension;
    }

    public String mimeType() {
        return mimeType;
    }

    public static Optional<ImageFormat> detect(byte[] content) {
        return Arrays.stream(values())
                .filter(format -> content.length >= format.signature.length
                        && Arrays.equals(content, 0, format.signature.length, format.signature, 0, format.signature.length))
                .findFirst();
    }
}
