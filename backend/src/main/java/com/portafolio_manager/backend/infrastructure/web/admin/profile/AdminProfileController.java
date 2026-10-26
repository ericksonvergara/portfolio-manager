package com.portafolio_manager.backend.infrastructure.web.admin.profile;

import com.portafolio_manager.backend.application.port.in.profile.GetPublicProfileUseCase;
import com.portafolio_manager.backend.application.port.in.profile.RemoveProfilePhotoUseCase;
import com.portafolio_manager.backend.application.port.in.profile.SetProfilePhotoUseCase;
import com.portafolio_manager.backend.application.port.in.profile.UpdateProfileUseCase;
import com.portafolio_manager.backend.domain.image.ImageUpload;
import com.portafolio_manager.backend.infrastructure.web.profile.ProfileResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;

@RestController
@RequestMapping("/api/admin/profile")
class AdminProfileController implements AdminProfileApi {

    private final GetPublicProfileUseCase getProfileUseCase;
    private final UpdateProfileUseCase updateProfileUseCase;
    private final SetProfilePhotoUseCase setProfilePhotoUseCase;
    private final RemoveProfilePhotoUseCase removeProfilePhotoUseCase;

    AdminProfileController(
            GetPublicProfileUseCase getProfileUseCase,
            UpdateProfileUseCase updateProfileUseCase,
            SetProfilePhotoUseCase setProfilePhotoUseCase,
            RemoveProfilePhotoUseCase removeProfilePhotoUseCase
    ) {
        this.getProfileUseCase = getProfileUseCase;
        this.updateProfileUseCase = updateProfileUseCase;
        this.setProfilePhotoUseCase = setProfilePhotoUseCase;
        this.removeProfilePhotoUseCase = removeProfilePhotoUseCase;
    }

    @Override
    @GetMapping
    public ProfileResponse getProfile() {
        return ProfileResponse.from(getProfileUseCase.execute(null));
    }

    @Override
    @PutMapping
    public ProfileResponse updateProfile(@Valid @RequestBody ProfileRequest request) {
        return ProfileResponse.from(updateProfileUseCase.execute(request.toDraft()));
    }

    @Override
    @PutMapping(path = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProfileResponse setPhoto(@RequestPart("file") MultipartFile file) {
        return ProfileResponse.from(setProfilePhotoUseCase.execute(upload(file)));
    }

    @Override
    @DeleteMapping("/photo")
    public ProfileResponse removePhoto() {
        return ProfileResponse.from(removeProfilePhotoUseCase.execute(null));
    }

    private static ImageUpload upload(MultipartFile file) {
        try {
            return new ImageUpload(file.getBytes(), file.getOriginalFilename(), file.getContentType());
        } catch (IOException exception) {
            throw new UncheckedIOException("Cannot read the uploaded file", exception);
        }
    }
}
