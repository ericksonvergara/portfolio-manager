package com.portafolio_manager.backend.infrastructure.storage;

import com.portafolio_manager.backend.domain.image.ImageFormat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.util.unit.DataSize;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;

import static org.assertj.core.api.Assertions.assertThat;

class LocalImageStorageTest {

    @TempDir
    private Path dir;

    @Test
    void shouldStoreEachImageUnderAUniqueName() throws Exception {
        LocalImageStorage storage = storage();

        String first = storage.store(TestImages.PNG, ImageFormat.PNG);
        String second = storage.store(TestImages.PNG, ImageFormat.PNG);

        assertThat(first).matches("/uploads/[0-9a-f-]{36}\\.png").isNotEqualTo(second);
        Path file = dir.resolve(first.substring("/uploads/".length()));
        assertThat(Files.readAllBytes(file)).isEqualTo(TestImages.PNG);
        // nginx reads the volume as another user
        assertThat(PosixFilePermissions.toString(Files.getPosixFilePermissions(file))).isEqualTo("rw-r--r--");
        try (var files = Files.list(dir)) {
            assertThat(files).hasSize(2); // no temporary file left
        }
    }

    @Test
    void shouldDeleteOnlyItsOwnFiles() throws Exception {
        LocalImageStorage storage = storage();
        String url = storage.store(TestImages.JPEG, ImageFormat.JPEG);
        Path outside = Files.writeString(dir.resolveSibling(dir.getFileName() + "-secret.txt"), "keep");

        storage.delete("/demo/projects/erp.svg");
        storage.delete("/uploads/../" + outside.getFileName());
        storage.delete(null);
        assertThat(outside).exists();

        storage.delete(url);
        storage.delete(url); // already gone: ignored
        try (var files = Files.list(dir)) {
            assertThat(files).isEmpty();
        }
        Files.delete(outside);
    }

    private LocalImageStorage storage() {
        return new LocalImageStorage(new UploadProperties(dir, DataSize.ofMegabytes(5)));
    }
}
