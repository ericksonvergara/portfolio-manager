package com.portafolio_manager.backend.infrastructure.storage;

import com.portafolio_manager.backend.application.port.out.image.ImageStoragePort;
import com.portafolio_manager.backend.domain.image.ImageFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Keeps the images in a local directory (a Docker volume) that nginx serves under /uploads/.
 * Files are named with a random UUID (RN007), so names never collide and cannot be guessed.
 */
@Component
@EnableConfigurationProperties(UploadProperties.class)
class LocalImageStorage implements ImageStoragePort {

    static final String PUBLIC_PATH = "/uploads/";

    private static final Logger log = LoggerFactory.getLogger(LocalImageStorage.class);
    private static final Pattern OWN_URL = Pattern.compile(
            "^/uploads/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(?:jpg|png))$");

    private static final Set<PosixFilePermission> PUBLIC_READ = PosixFilePermissions.fromString("rw-r--r--");

    private final Path dir;

    LocalImageStorage(UploadProperties properties) {
        this.dir = properties.dir().toAbsolutePath().normalize();
        try {
            Files.createDirectories(dir);
        } catch (IOException exception) {
            throw new UncheckedIOException("Cannot create the uploads directory " + dir, exception);
        }
    }

    @Override
    public String store(byte[] content, ImageFormat format) {
        String name = UUID.randomUUID() + "." + format.extension();
        try {
            // Written next to its final place and then moved, so nginx never serves a half-written file.
            Path temporary = Files.createTempFile(dir, "upload-", ".tmp");
            Files.write(temporary, content);
            makeReadableByTheWebServer(temporary);
            Files.move(temporary, dir.resolve(name), StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException exception) {
            throw new UncheckedIOException("Cannot store the image", exception);
        }
        return PUBLIC_PATH + name;
    }

    /*
     * Temporary files are created owner-only (rw-------), but nginx reads the volume as another user.
     * Uploaded images are public anyway, so they get rw-r--r--.
     */
    private static void makeReadableByTheWebServer(Path file) throws IOException {
        try {
            Files.setPosixFilePermissions(file, PUBLIC_READ);
        } catch (UnsupportedOperationException ignored) {
            // Not a POSIX file system (for example Windows): permissions do not apply.
        }
    }

    @Override
    public void delete(String url) {
        Matcher own = OWN_URL.matcher(url == null ? "" : url);
        if (!own.matches()) {
            return;
        }
        try {
            Files.deleteIfExists(dir.resolve(own.group(1)));
        } catch (IOException exception) {
            // The image is no longer referenced: a leftover file is harmless, so it does not fail the request.
            log.warn("Could not delete the image {}", url, exception);
        }
    }
}
