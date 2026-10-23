package com.portafolio_manager.backend.application.service.technology;

import com.portafolio_manager.backend.application.port.in.technology.ListAdminTechnologiesUseCase;
import com.portafolio_manager.backend.application.port.out.technology.TechnologyRepositoryPort;
import com.portafolio_manager.backend.domain.technology.AdminTechnology;

import java.util.List;

public class ListAdminTechnologiesService implements ListAdminTechnologiesUseCase {

    private final TechnologyRepositoryPort technologyRepository;

    public ListAdminTechnologiesService(TechnologyRepositoryPort technologyRepository) {
        this.technologyRepository = technologyRepository;
    }

    @Override
    public List<AdminTechnology> execute(Void input) {
        return technologyRepository.findAllForAdmin();
    }
}
