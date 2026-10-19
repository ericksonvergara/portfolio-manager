package com.portafolio_manager.backend.infrastructure.persistence.profile;

import com.portafolio_manager.backend.domain.profile.SocialPlatform;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "social_links")
public class SocialLinkJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SocialPlatform platform;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    protected SocialLinkJpaEntity() {
    }

    public SocialPlatform getPlatform() {
        return platform;
    }

    public String getUrl() {
        return url;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }
}
