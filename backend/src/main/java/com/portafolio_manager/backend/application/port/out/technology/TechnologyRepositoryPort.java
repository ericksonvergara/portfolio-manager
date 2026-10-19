package com.portafolio_manager.backend.application.port.out.technology;

import com.portafolio_manager.backend.application.port.out.PersistencePort;
import com.portafolio_manager.backend.domain.technology.Technology;

import java.util.List;

public interface TechnologyRepositoryPort extends PersistencePort {

    /** Every technology, by display order and then by name. */
    List<Technology> findAllInDisplayOrder();
}
