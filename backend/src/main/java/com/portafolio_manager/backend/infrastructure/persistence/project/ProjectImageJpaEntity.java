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

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "alt_text", length = 200)
    private String altText;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    protected ProjectImageJpaEntity() {
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
