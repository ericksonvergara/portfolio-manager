package com.portafolio_manager.backend.domain.image;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImageUploadTest {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0};
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0};
    private static final long MAX = 1024;

    @Test
    void shouldAcceptJpgJpegAndPngFiles() {
        assertThat(new ImageUpload(PNG, "portada.PNG", "image/png").validate(MAX)).isEqualTo(ImageFormat.PNG);
        assertThat(new ImageUpload(JPEG, "foto.jpg", "image/jpeg").validate(MAX)).isEqualTo(ImageFormat.JPEG);
        assertThat(new ImageUpload(JPEG, "foto.jpeg", "image/jpeg").validate(MAX)).isEqualTo(ImageFormat.JPEG);
    }

    @Test
    void shouldRejectAnEmptyOrTooLargeFile() {
        assertThatThrownBy(() -> new ImageUpload(new byte[0], "a.png", "image/png").validate(MAX))
                .isInstanceOf(InvalidImageException.class)
                .hasMessage("The file is empty");
        assertThatThrownBy(() -> new ImageUpload(new byte[2048], "a.png", "image/png").validate(MAX))
                .isInstanceOf(ImageTooLargeException.class);
    }

    @Test
    void shouldRejectOtherExtensions() {
        assertThatThrownBy(() -> new ImageUpload(PNG, "animacion.gif", "image/gif").validate(MAX))
                .isInstanceOf(InvalidImageException.class)
                .hasMessage("Only JPG, JPEG and PNG files are allowed");
        assertThatThrownBy(() -> new ImageUpload(PNG, "sin-extension", "image/png").validate(MAX))
                .isInstanceOf(InvalidImageException.class);
    }

    @Test
    void shouldRejectAFileWhoseContentIsNotAnImage() {
        byte[] script = "<script>alert(1)</script>".getBytes(StandardCharsets.UTF_8);

        assertThatThrownBy(() -> new ImageUpload(script, "falsa.png", "image/png").validate(MAX))
                .isInstanceOf(InvalidImageException.class)
                .hasMessage("The file content is not a JPG or PNG image");
    }

    @Test
    void shouldRejectAMismatchBetweenNameTypeAndContent() {
        // A JPEG renamed to .png, and a PNG declared as JPEG
        assertThatThrownBy(() -> new ImageUpload(JPEG, "foto.png", "image/png").validate(MAX))
                .hasMessage("The file type does not match its content");
        assertThatThrownBy(() -> new ImageUpload(PNG, "portada.png", "image/jpeg").validate(MAX))
                .hasMessage("The file type does not match its content");
    }
}
