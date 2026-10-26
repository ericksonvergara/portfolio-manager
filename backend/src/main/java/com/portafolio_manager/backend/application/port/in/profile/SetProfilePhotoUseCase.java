package com.portafolio_manager.backend.application.port.in.profile;

import com.portafolio_manager.backend.application.port.in.UseCase;
import com.portafolio_manager.backend.domain.image.ImageUpload;
import com.portafolio_manager.backend.domain.profile.Profile;

/**
 * Uploads or replaces the profile photo (HU023); the replaced file is deleted. Throws
 * {@code ProfileNotFoundException} while the profile is not saved yet, or {@code InvalidImageException}.
 */
public interface SetProfilePhotoUseCase extends UseCase<ImageUpload, Profile> {
}
