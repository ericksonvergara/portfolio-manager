package com.portafolio_manager.backend.infrastructure.web.project;

import com.portafolio_manager.backend.application.port.in.project.ListPublishedProjectsUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/projects")
class PublicProjectController implements PublicProjectApi {

    private final ListPublishedProjectsUseCase listPublishedProjectsUseCase;

    PublicProjectController(ListPublishedProjectsUseCase listPublishedProjectsUseCase) {
        this.listPublishedProjectsUseCase = listPublishedProjectsUseCase;
    }

    @Override
    @GetMapping
    public List<ProjectCardResponse> listProjects() {
        return listPublishedProjectsUseCase.execute(null).stream().map(ProjectCardResponse::from).toList();
    }
}
