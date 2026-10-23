package com.portafolio_manager.backend.infrastructure.persistence.technology;

import com.portafolio_manager.backend.application.port.out.technology.TechnologyRepositoryPort;
import com.portafolio_manager.backend.domain.technology.AdminTechnology;
import com.portafolio_manager.backend.domain.technology.DuplicateTechnologyNameException;
import com.portafolio_manager.backend.domain.technology.Technology;
import com.portafolio_manager.backend.domain.technology.TechnologyDraft;
import com.portafolio_manager.backend.domain.technology.TechnologyInUseException;
import com.portafolio_manager.backend.domain.technology.TechnologyUpdate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    @Override
    @Transactional(readOnly = true)
    public List<AdminTechnology> findAllForAdmin() {
        Map<Long, Long> projects = technologyRepository.countProjectsByTechnology().stream()
                .collect(Collectors.toMap(row -> ((Number) row[0]).longValue(), row -> ((Number) row[1]).longValue()));
        return technologyRepository.findAllByOrderByDisplayOrderAscNameAsc().stream()
                .map(entity -> toAdmin(entity, projects.getOrDefault(entity.getId(), 0L)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AdminTechnology> findForAdmin(Long id) {
        return technologyRepository.findById(id)
                .map(entity -> toAdmin(entity, technologyRepository.countProjects(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNameIgnoringCase(String name, Long excludedId) {
        return technologyRepository.existsByNameIgnoringCase(name, excludedId);
    }

    @Override
    @Transactional
    public AdminTechnology create(TechnologyDraft draft) {
        try {
            return toAdmin(technologyRepository.saveAndFlush(TechnologyJpaEntity.newTechnology(draft)), 0);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateTechnologyNameException(draft.name());
        }
    }

    @Override
    @Transactional
    public Optional<AdminTechnology> update(TechnologyUpdate update) {
        Optional<TechnologyJpaEntity> found = technologyRepository.findById(update.id());
        if (found.isEmpty()) {
            return Optional.empty();
        }
        TechnologyJpaEntity entity = found.get();
        entity.apply(update.draft());
        try {
            technologyRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateTechnologyNameException(update.draft().name());
        }
        return Optional.of(toAdmin(entity, technologyRepository.countProjects(update.id())));
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        Optional<TechnologyJpaEntity> found = technologyRepository.findById(id);
        if (found.isEmpty()) {
            return false;
        }
        try {
            technologyRepository.delete(found.get());
            technologyRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            // A project started using it after the check: project_technologies does not cascade.
            throw new TechnologyInUseException(found.get().getName(), 1);
        }
        return true;
    }

    private static AdminTechnology toAdmin(TechnologyJpaEntity entity, long projectCount) {
        return new AdminTechnology(
                entity.getId(), entity.getName(), entity.getCategory(), entity.getDisplayOrder(), projectCount);
    }
}
