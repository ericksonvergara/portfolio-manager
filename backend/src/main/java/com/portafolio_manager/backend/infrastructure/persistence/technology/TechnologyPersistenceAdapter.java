package com.portafolio_manager.backend.infrastructure.persistence.technology;

import com.portafolio_manager.backend.application.port.out.technology.TechnologyRepositoryPort;
import com.portafolio_manager.backend.domain.technology.Technology;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
class TechnologyPersistenceAdapter implements TechnologyRepositoryPort {

    private final TechnologyJpaRepository technologyRepository;

    TechnologyPersistenceAdapter(TechnologyJpaRepository technologyRepository) {
        this.technologyRepository = technologyRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Technology> findAllInDisplayOrder() {
        return technologyRepository.findAllByOrderByDisplayOrderAscNameAsc().stream()
                .map(entity -> new Technology(entity.getName(), entity.getCategory()))
                .toList();
    }
}
