package com.portafolio_manager.backend.infrastructure.persistence.profile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Read-only for now: the profile is created by a migration and becomes editable with the
 * administrator profile story, which will add the write side.
 */
@Entity
@Table(name = "profiles")
public class ProfileJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, length = 150)
    private String headline;

    @Column(nullable = false, length = 2000)
    private String about;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(length = 100)
    private String location;

    @OneToMany
    @JoinColumn(name = "profile_id")
    @OrderBy("displayOrder ASC")
    private List<SocialLinkJpaEntity> socialLinks = new ArrayList<>();

    protected ProfileJpaEntity() {
    }

    public String getFullName() {
        return fullName;
    }

    public String getHeadline() {
        return headline;
    }

    public String getAbout() {
        return about;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getEmail() {
        return email;
    }

    public String getLocation() {
        return location;
    }

    public List<SocialLinkJpaEntity> getSocialLinks() {
        return socialLinks;
    }
}
