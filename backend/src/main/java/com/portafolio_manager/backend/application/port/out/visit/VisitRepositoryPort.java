package com.portafolio_manager.backend.application.port.out.visit;

import com.portafolio_manager.backend.application.port.out.PersistencePort;
import com.portafolio_manager.backend.domain.visit.Visit;

public interface VisitRepositoryPort extends PersistencePort {

    void save(Visit visit);
}
