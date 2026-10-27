package com.portafolio_manager.backend.application.service.profile;

import com.portafolio_manager.backend.application.port.out.image.ImageStoragePort;
import com.portafolio_manager.backend.application.port.out.profile.ProfileRepositoryPort;
import com.portafolio_manager.backend.domain.image.ImageFormat;
import com.portafolio_manager.backend.domain.image.ImageUpload;
import com.portafolio_manager.backend.domain.image.InvalidImageException;
import com.portafolio_manager.backend.domain.profile.Profile;
import com.portafolio_manager.backend.domain.profile.ProfileDraft;
import com.portafolio_manager.backend.domain.profile.ProfileNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Profile administration (HU023): the photo services keep the stored files in step with the database.
 */
class ProfileAdminServicesTest {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0};
    private static final ImageUpload VALID = new ImageUpload(PNG, "foto.png", "image/png");

    private final ProfileRepositoryPort profileRepository = mock(ProfileRepositoryPort.class);
    private final ImageStoragePort storage = mock(ImageStoragePort.class);

    @Test
    void shouldSaveTheDraft() {
        ProfileDraft draft = new ProfileDraft("Erickson", "Titular", "Presentación", "contacto@example.com", null, List.of());
        when(profileRepository.save(draft)).thenReturn(profile(null));

        assertThat(new UpdateProfileService(profileRepository).execute(draft)).isEqualTo(profile(null));
    }

    @Test
    void shouldReplaceThePhotoAndDeleteThePreviousFile() {
        when(profileRepository.find()).thenReturn(Optional.of(profile("/uploads/old.png")));
        when(storage.store(PNG, ImageFormat.PNG)).thenReturn("/uploads/new.png");
        when(profileRepository.setPhoto("/uploads/new.png")).thenReturn(Optional.of(profile("/uploads/new.png")));

        Profile result = new SetProfilePhotoService(profileRepository, storage, 1024).execute(VALID);

        assertThat(result.photoUrl()).isEqualTo("/uploads/new.png");
        verify(storage).delete("/uploads/old.png");
    }

    @Test
    void shouldDeleteTheNewFileWhenTheProfileCannotBeUpdated() {
        when(profileRepository.find()).thenReturn(Optional.of(profile(null)));
        when(storage.store(PNG, ImageFormat.PNG)).thenReturn("/uploads/new.png");
        when(profileRepository.setPhoto("/uploads/new.png")).thenThrow(new IllegalStateException("database down"));

        assertThatThrownBy(() -> new SetProfilePhotoService(profileRepository, storage, 1024).execute(VALID))
                .isInstanceOf(IllegalStateException.class);
        verify(storage).delete("/uploads/new.png");
    }

    @Test
    void shouldNeitherStoreAnInvalidFileNorAPhotoWithoutProfile() {
        when(profileRepository.find()).thenReturn(Optional.of(profile(null)));
        ImageUpload fake = new ImageUpload("not an image".getBytes(), "falsa.png", "image/png");
        assertThatThrownBy(() -> new SetProfilePhotoService(profileRepository, storage, 1024).execute(fake))
                .isInstanceOf(InvalidImageException.class);

        when(profileRepository.find()).thenReturn(Optional.empty());
        assertThatThrownBy(() -> new SetProfilePhotoService(profileRepository, storage, 1024).execute(VALID))
                .isInstanceOf(ProfileNotFoundException.class);

        verifyNoInteractions(storage);
    }

    @Test
    void shouldRemoveThePhotoAndItsFile() {
        when(profileRepository.find()).thenReturn(Optional.of(profile("/uploads/old.png")));
        when(profileRepository.setPhoto(null)).thenReturn(Optional.of(profile(null)));

        assertThat(new RemoveProfilePhotoService(profileRepository, storage).execute(null).photoUrl()).isNull();
        verify(storage).delete("/uploads/old.png");
    }

    @Test
    void shouldDoNothingWhenThereIsNoPhotoToRemove() {
        when(profileRepository.find()).thenReturn(Optional.of(profile(null)));

        assertThat(new RemoveProfilePhotoService(profileRepository, storage).execute(null)).isEqualTo(profile(null));
        verify(profileRepository, never()).setPhoto(any());
        verifyNoInteractions(storage);
    }

    private static Profile profile(String photoUrl) {
        return new Profile("Erickson", "Titular", "Presentación", photoUrl, "contacto@example.com", null, List.of());
    }
}
