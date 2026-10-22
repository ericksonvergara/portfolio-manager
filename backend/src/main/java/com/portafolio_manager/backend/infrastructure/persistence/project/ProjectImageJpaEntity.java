package com.portafolio_manager.backend.infrastructure.persistence.project;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "project_images")
public class ProjectImageJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "alt_text", length = 200)
    private String altText;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    protected ProjectImageJpaEntity() {
    }

    static ProjectImageJpaEntity newImage(Long projectId, String imageUrl, String altText, int displayOrder) {
        ProjectImageJpaEntity image = new ProjectImageJpaEntity();
        image.projectId = projectId;
        image.imageUrl = imageUrl;
        image.altText = altText;
        image.displayOrder = displayOrder;
        return image;
    }

    public Long getId() {
        return id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getAltText() {
        return altText;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }
}
