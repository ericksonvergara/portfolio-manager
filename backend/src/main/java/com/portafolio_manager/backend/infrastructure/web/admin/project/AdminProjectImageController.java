package com.portafolio_manager.backend.infrastructure.web.admin.project;

import com.portafolio_manager.backend.application.port.in.project.AddGalleryImageUseCase;
import com.portafolio_manager.backend.application.port.in.project.RemoveGalleryImageUseCase;
import com.portafolio_manager.backend.application.port.in.project.RemoveProjectCoverUseCase;
import com.portafolio_manager.backend.application.port.in.project.SetProjectCoverUseCase;
import com.portafolio_manager.backend.domain.image.ImageUpload;
import com.portafolio_manager.backend.domain.project.GalleryImageRef;
import com.portafolio_manager.backend.domain.project.ImageChange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;

@RestController
@RequestMapping("/api/admin/projects/{id}")
class AdminProjectImageController implements AdminProjectImageApi {

    private final SetProjectCoverUseCase setProjectCoverUseCase;
    private final RemoveProjectCoverUseCase removeProjectCoverUseCase;
    private final AddGalleryImageUseCase addGalleryImageUseCase;
    private final RemoveGalleryImageUseCase removeGalleryImageUseCase;

    AdminProjectImageController(
            SetProjectCoverUseCase setProjectCoverUseCase,
            RemoveProjectCoverUseCase removeProjectCoverUseCase,
            AddGalleryImageUseCase addGalleryImageUseCase,
            RemoveGalleryImageUseCase removeGalleryImageUseCase
    ) {
        this.setProjectCoverUseCase = setProjectCoverUseCase;
        this.removeProjectCoverUseCase = removeProjectCoverUseCase;
        this.addGalleryImageUseCase = addGalleryImageUseCase;
        this.removeGalleryImageUseCase = removeGalleryImageUseCase;
    }

    @Override
    @PutMapping(path = "/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AdminProjectResponse setCover(@PathVariable Long id, @RequestPart("file") MultipartFile file) {
        return AdminProjectResponse.from(setProjectCoverUseCase.execute(new ImageChange(id, upload(file), null)));
    }

    @Override
    @DeleteMapping("/cover")
    public AdminProjectResponse removeCover(@PathVariable Long id) {
        return AdminProjectResponse.from(removeProjectCoverUseCase.execute(id));
    }

    @Override
    @PostMapping(path = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AdminProjectResponse> addGalleryImage(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file,
            @RequestParam(name = "altText", required = false) String altText
    ) {
        AdminProjectResponse project = AdminProjectResponse.from(
                addGalleryImageUseCase.execute(new ImageChange(id, upload(file), altText)));
        return ResponseEntity.status(HttpStatus.CREATED).body(project);
    }

    @Override
    @DeleteMapping("/images/{imageId}")
    public AdminProjectResponse removeGalleryImage(@PathVariable Long id, @PathVariable Long imageId) {
        return AdminProjectResponse.from(removeGalleryImageUseCase.execute(new GalleryImageRef(id, imageId)));
    }

    private static ImageUpload upload(MultipartFile file) {
        try {
            return new ImageUpload(file.getBytes(), file.getOriginalFilename(), file.getContentType());
        } catch (IOException exception) {
            throw new UncheckedIOException("Cannot read the uploaded file", exception);
        }
    }
}
