package com.portafolio_manager.backend.infrastructure.web.admin.project;

import com.portafolio_manager.backend.application.port.in.project.CreateProjectUseCase;
import com.portafolio_manager.backend.application.port.in.project.ListAdminProjectsUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/admin/projects")
class AdminProjectController implements AdminProjectApi {

    private final ListAdminProjectsUseCase listAdminProjectsUseCase;
    private final CreateProjectUseCase createProjectUseCase;

    AdminProjectController(ListAdminProjectsUseCase listAdminProjectsUseCase, CreateProjectUseCase createProjectUseCase) {
        this.listAdminProjectsUseCase = listAdminProjectsUseCase;
        this.createProjectUseCase = createProjectUseCase;
    }

    @Override
    @GetMapping
    public List<AdminProjectResponse> listProjects() {
        return listAdminProjectsUseCase.execute(null).stream().map(AdminProjectResponse::from).toList();
    }

    @Override
    @PostMapping
    public ResponseEntity<AdminProjectResponse> createProject(@Valid @RequestBody ProjectRequest request) {
        AdminProjectResponse created = AdminProjectResponse.from(createProjectUseCase.execute(request.toDraft()));
        return ResponseEntity.created(URI.create("/api/admin/projects/" + created.id())).body(created);
    }
}
