package com.portafolio_manager.backend.infrastructure.web.project;

import com.portafolio_manager.backend.application.port.in.project.GetPublishedProjectUseCase;
import com.portafolio_manager.backend.application.port.in.project.ListPublishedProjectsUseCase;
import com.portafolio_manager.backend.domain.project.ProjectSearch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/projects")
class PublicProjectController implements PublicProjectApi {

    private final ListPublishedProjectsUseCase listPublishedProjectsUseCase;
    private final GetPublishedProjectUseCase getPublishedProjectUseCase;

    PublicProjectController(
            ListPublishedProjectsUseCase listPublishedProjectsUseCase,
            GetPublishedProjectUseCase getPublishedProjectUseCase
    ) {
        this.listPublishedProjectsUseCase = listPublishedProjectsUseCase;
        this.getPublishedProjectUseCase = getPublishedProjectUseCase;
    }

    @Override
    @GetMapping
    public List<ProjectCardResponse> listProjects(@RequestParam(name = "search", required = false) String search) {
        return listPublishedProjectsUseCase.execute(new ProjectSearch(search)).stream()
                .map(ProjectCardResponse::from)
                .toList();
    }

    @Override
    @GetMapping("/{slug}")
    public ProjectDetailResponse getProject(@PathVariable String slug) {
        return ProjectDetailResponse.from(getPublishedProjectUseCase.execute(slug));
    }
}
