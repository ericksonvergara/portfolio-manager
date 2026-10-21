package com.portafolio_manager.backend.application.port.out.technology;

import com.portafolio_manager.backend.application.port.out.PersistencePort;
import com.portafolio_manager.backend.domain.technology.Technology;

import java.util.List;
import java.util.Set;

public interface TechnologyRepositoryPort extends PersistencePort {

    /** Every technology, by display order and then by name. */
    List<Technology> findAllInDisplayOrder();

    /** The ids of the given set that exist in the catalog. */
    Set<Long> findExistingIds(Set<Long> ids);
}
