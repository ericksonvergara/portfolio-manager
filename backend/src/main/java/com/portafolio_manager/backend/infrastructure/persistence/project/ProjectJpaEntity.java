package com.portafolio_manager.backend.infrastructure.persistence.project;

import com.portafolio_manager.backend.domain.project.ProjectStatus;
import com.portafolio_manager.backend.infrastructure.persistence.technology.TechnologyJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Read-only for now: projects become editable with the project management stories (Sprint 4).
 */
@Entity
@Table(name = "projects")
public class ProjectJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 120)
    private String slug;

    @Column(name = "short_description", nullable = false, length = 300)
    private String shortDescription;

    @Column(nullable = false, length = 5000)
    private String description;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Column(name = "project_url", length = 500)
    private String projectUrl;

    @Column(name = "repository_url", length = 500)
    private String repositoryUrl;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectStatus status;

    @Column(name = "developed_on")
    private LocalDate developedOn;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private boolean featured;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @ManyToMany
    @JoinTable(
            name = "project_technologies",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "technology_id")
    )
    @OrderBy("displayOrder ASC, name ASC")
    private List<TechnologyJpaEntity> technologies = new ArrayList<>();

    @OneToMany
    @JoinColumn(name = "project_id")
    @OrderBy("displayOrder ASC, id ASC")
    private List<ProjectImageJpaEntity> images = new ArrayList<>();

    protected ProjectJpaEntity() {
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getDescription() {
        return description;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public String getProjectUrl() {
        return projectUrl;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public LocalDate getDevelopedOn() {
        return developedOn;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isFeatured() {
        return featured;
    }

    public List<TechnologyJpaEntity> getTechnologies() {
        return technologies;
    }

    public List<ProjectImageJpaEntity> getImages() {
        return images;
    }
}
