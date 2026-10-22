package com.portafolio_manager.backend.infrastructure.storage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A temporary uploads directory for the integration tests, and minimal image contents.
 */
public final class TestImages {

    public static final Path UPLOADS_DIR = createUploadsDir();

    /** A real 1x1 PNG. */
    public static final byte[] PNG = {
            (byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0, 0, 13, 'I', 'H', 'D', 'R', 0, 0, 0, 1, 0, 0, 0, 1,
            8, 6, 0, 0, 0, 0x1F, 0x15, (byte) 0xC4, (byte) 0x89, 0, 0, 0, 13, 'I', 'D', 'A', 'T', 0x78, (byte) 0x9C, 99,
            0, 1, 0, 0, 5, 0, 1, 0x0D, 0x0A, 0x2D, (byte) 0xB4, 0, 0, 0, 0, 'I', 'E', 'N', 'D', (byte) 0xAE, 0x42, 0x60,
            (byte) 0x82
    };

    /** The start of a JPEG file. */
    public static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 16, 'J', 'F', 'I', 'F', 0};

    private TestImages() {
    }

    private static Path createUploadsDir() {
        try {
            Path dir = Files.createTempDirectory("portfolio-uploads-");
            dir.toFile().deleteOnExit();
            return dir;
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
