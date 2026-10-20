package com.portafolio_manager.backend.application.service.technology;

import com.portafolio_manager.backend.application.port.in.technology.ListTechnologiesUseCase;
import com.portafolio_manager.backend.application.port.out.technology.TechnologyRepositoryPort;
import com.portafolio_manager.backend.domain.technology.Technology;

import java.util.List;

public class ListTechnologiesService implements ListTechnologiesUseCase {

    private final TechnologyRepositoryPort technologyRepository;

    public ListTechnologiesService(TechnologyRepositoryPort technologyRepository) {
        this.technologyRepository = technologyRepository;
    }

    @Override
    public List<Technology> execute(Void input) {
        return technologyRepository.findAllInDisplayOrder();
    }
}
