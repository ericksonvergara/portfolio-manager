package com.portafolio_manager.backend.infrastructure.persistence.profile;

import com.portafolio_manager.backend.domain.profile.ProfileDraft;
import com.portafolio_manager.backend.domain.profile.SocialLink;
import com.portafolio_manager.backend.domain.profile.SocialPlatform;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The single public profile. The demo seed creates it locally; elsewhere the administrator creates it
 * the first time the profile is saved (HU023).
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

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "profile_id", nullable = false)
    @OrderBy("displayOrder ASC")
    private List<SocialLinkJpaEntity> socialLinks = new ArrayList<>();

    protected ProfileJpaEntity() {
    }

    static ProfileJpaEntity newProfile(ProfileDraft draft) {
        ProfileJpaEntity entity = new ProfileJpaEntity();
        entity.apply(draft);
        return entity;
    }

    void apply(ProfileDraft draft) {
        fullName = draft.fullName();
        headline = draft.headline();
        about = draft.about();
        email = draft.email();
        location = draft.location();
        updatedAt = Instant.now();

        // Links are updated in place by platform: removing and re-inserting one would break the unique
        // (profile_id, platform) constraint, because Hibernate runs the inserts before the deletes.
        Map<SocialPlatform, SocialLinkJpaEntity> current = socialLinks.stream()
                .collect(Collectors.toMap(SocialLinkJpaEntity::getPlatform, Function.identity()));
        List<SocialLinkJpaEntity> links = new ArrayList<>();
        int displayOrder = 1;
        for (SocialLink link : draft.socialLinks()) {
            SocialLinkJpaEntity entity = current.get(link.platform());
            if (entity == null) {
                entity = new SocialLinkJpaEntity(link.platform());
            }
            entity.apply(link.url(), displayOrder++);
            links.add(entity);
        }
        socialLinks.clear();
        socialLinks.addAll(links);
    }

    void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
        updatedAt = Instant.now();
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
