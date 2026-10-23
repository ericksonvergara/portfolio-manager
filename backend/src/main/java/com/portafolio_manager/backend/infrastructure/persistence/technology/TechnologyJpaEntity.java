package com.portafolio_manager.backend.infrastructure.persistence.technology;

import com.portafolio_manager.backend.domain.technology.TechnologyCategory;
import com.portafolio_manager.backend.domain.technology.TechnologyDraft;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * The catalog, managed from the admin panel (HU021). Projects reference it by id (HU015).
 */
@Entity
@Table(name = "technologies")
public class TechnologyJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TechnologyCategory category;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected TechnologyJpaEntity() {
    }

    static TechnologyJpaEntity newTechnology(TechnologyDraft draft) {
        TechnologyJpaEntity entity = new TechnologyJpaEntity();
        entity.apply(draft);
        return entity;
    }

    void apply(TechnologyDraft draft) {
        name = draft.name();
        category = draft.category();
        displayOrder = draft.displayOrder();
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public TechnologyCategory getCategory() {
        return category;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }
}
