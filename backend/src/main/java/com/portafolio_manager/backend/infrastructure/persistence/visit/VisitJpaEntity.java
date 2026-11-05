package com.portafolio_manager.backend.infrastructure.persistence.visit;

import com.portafolio_manager.backend.domain.visit.Visit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "visits")
public class VisitJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "visited_at", nullable = false)
    private Instant visitedAt;

    @Column(name = "anonymized_ip", nullable = false, length = 45)
    private String anonymizedIp;

    @Column(nullable = false, length = 30)
    private String browser;

    @Column(name = "operating_system", nullable = false, length = 30)
    private String operatingSystem;

    @Column(nullable = false, length = 20)
    private String device;

    @Column(length = 2)
    private String country;

    /** A plain id, not a relation: a visit never loads its project, and survives it (ON DELETE SET NULL). */
    @Column(name = "project_id")
    private Long projectId;

    protected VisitJpaEntity() {
    }

    static VisitJpaEntity from(Visit visit) {
        VisitJpaEntity entity = new VisitJpaEntity();
        entity.visitedAt = visit.visitedAt();
        entity.anonymizedIp = visit.anonymizedIp();
        entity.browser = visit.agent().browser();
        entity.operatingSystem = visit.agent().operatingSystem();
        entity.device = visit.agent().device();
        entity.country = visit.country();
        entity.projectId = visit.projectId();
        return entity;
    }
}
