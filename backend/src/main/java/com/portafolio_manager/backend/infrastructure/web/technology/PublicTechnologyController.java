package com.portafolio_manager.backend.infrastructure.web.technology;

import com.portafolio_manager.backend.application.port.in.technology.ListTechnologiesUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/technologies")
class PublicTechnologyController implements PublicTechnologyApi {

    private final ListTechnologiesUseCase listTechnologiesUseCase;

    PublicTechnologyController(ListTechnologiesUseCase listTechnologiesUseCase) {
        this.listTechnologiesUseCase = listTechnologiesUseCase;
    }

    @Override
    @GetMapping
    public List<TechnologyResponse> listTechnologies() {
        return listTechnologiesUseCase.execute(null).stream().map(TechnologyResponse::from).toList();
    }
}
