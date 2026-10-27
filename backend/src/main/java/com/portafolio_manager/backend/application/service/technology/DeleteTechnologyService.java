package com.portafolio_manager.backend.application.service.technology;

import com.portafolio_manager.backend.application.port.in.technology.DeleteTechnologyUseCase;
import com.portafolio_manager.backend.application.port.out.technology.TechnologyRepositoryPort;
import com.portafolio_manager.backend.domain.technology.AdminTechnology;
import com.portafolio_manager.backend.domain.technology.TechnologyInUseException;
import com.portafolio_manager.backend.domain.technology.TechnologyNotFoundException;

public class DeleteTechnologyService implements DeleteTechnologyUseCase {

    private final TechnologyRepositoryPort technologyRepository;

    public DeleteTechnologyService(TechnologyRepositoryPort technologyRepository) {
        this.technologyRepository = technologyRepository;
    }

    @Override
    public Void execute(Long id) {
        AdminTechnology technology = technologyRepository.findForAdmin(id)
                .orElseThrow(() -> new TechnologyNotFoundException(id));
        if (technology.projectCount() > 0) {
            throw new TechnologyInUseException(technology.name(), technology.projectCount()); // RN005
        }
        if (!technologyRepository.delete(id)) {
            throw new TechnologyNotFoundException(id);
        }
        return null;
    }
}
