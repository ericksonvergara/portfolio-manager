package com.portafolio_manager.backend.infrastructure.persistence.technology;

import com.portafolio_manager.backend.domain.technology.TechnologyCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Read-only for now: the catalog becomes editable with the technology management story.
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

    protected TechnologyJpaEntity() {
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
