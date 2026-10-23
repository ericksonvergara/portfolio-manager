package com.portafolio_manager.backend.application.service.technology;

import com.portafolio_manager.backend.application.port.in.technology.UpdateTechnologyUseCase;
import com.portafolio_manager.backend.application.port.out.technology.TechnologyRepositoryPort;
import com.portafolio_manager.backend.domain.technology.AdminTechnology;
import com.portafolio_manager.backend.domain.technology.DuplicateTechnologyNameException;
import com.portafolio_manager.backend.domain.technology.TechnologyNotFoundException;
import com.portafolio_manager.backend.domain.technology.TechnologyUpdate;

public class UpdateTechnologyService implements UpdateTechnologyUseCase {

    private final TechnologyRepositoryPort technologyRepository;

    public UpdateTechnologyService(TechnologyRepositoryPort technologyRepository) {
        this.technologyRepository = technologyRepository;
    }

    @Override
    public AdminTechnology execute(TechnologyUpdate update) {
        if (technologyRepository.existsByNameIgnoringCase(update.draft().name(), update.id())) {
            throw new DuplicateTechnologyNameException(update.draft().name());
        }
        return technologyRepository.update(update).orElseThrow(() -> new TechnologyNotFoundException(update.id()));
    }
}
