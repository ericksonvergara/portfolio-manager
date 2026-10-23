package com.portafolio_manager.backend.application.service.technology;

import com.portafolio_manager.backend.application.port.in.technology.CreateTechnologyUseCase;
import com.portafolio_manager.backend.application.port.out.technology.TechnologyRepositoryPort;
import com.portafolio_manager.backend.domain.technology.AdminTechnology;
import com.portafolio_manager.backend.domain.technology.DuplicateTechnologyNameException;
import com.portafolio_manager.backend.domain.technology.TechnologyDraft;

public class CreateTechnologyService implements CreateTechnologyUseCase {

    private final TechnologyRepositoryPort technologyRepository;

    public CreateTechnologyService(TechnologyRepositoryPort technologyRepository) {
        this.technologyRepository = technologyRepository;
    }

    @Override
    public AdminTechnology execute(TechnologyDraft draft) {
        if (technologyRepository.existsByNameIgnoringCase(draft.name(), null)) {
            throw new DuplicateTechnologyNameException(draft.name());
        }
        return technologyRepository.create(draft);
    }
}
