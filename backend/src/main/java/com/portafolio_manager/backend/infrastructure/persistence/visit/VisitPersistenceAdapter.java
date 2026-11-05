package com.portafolio_manager.backend.infrastructure.persistence.visit;

import com.portafolio_manager.backend.application.port.out.visit.VisitRepositoryPort;
import com.portafolio_manager.backend.domain.visit.Visit;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class VisitPersistenceAdapter implements VisitRepositoryPort {

    private final VisitJpaRepository visitRepository;

    VisitPersistenceAdapter(VisitJpaRepository visitRepository) {
        this.visitRepository = visitRepository;
    }

    @Override
    @Transactional
    public void save(Visit visit) {
        visitRepository.save(VisitJpaEntity.from(visit));
    }
}
