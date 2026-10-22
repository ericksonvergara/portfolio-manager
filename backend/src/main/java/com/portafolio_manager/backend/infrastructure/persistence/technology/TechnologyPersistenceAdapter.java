package com.portafolio_manager.backend.infrastructure.persistence.technology;

import com.portafolio_manager.backend.application.port.out.technology.TechnologyRepositoryPort;
import com.portafolio_manager.backend.domain.technology.Technology;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
                .map(entity -> new Technology(entity.getId(), entity.getName(), entity.getCategory()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> findExistingIds(Set<Long> ids) {
        return technologyRepository.findAllById(ids).stream()
                .map(TechnologyJpaEntity::getId)
                .collect(Collectors.toSet());
    }
}
