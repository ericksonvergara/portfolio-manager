package com.portafolio_manager.backend.infrastructure.web.admin.project;

import com.portafolio_manager.backend.application.port.in.project.CreateProjectUseCase;
import com.portafolio_manager.backend.application.port.in.project.GetAdminProjectUseCase;
import com.portafolio_manager.backend.application.port.in.project.ListAdminProjectsUseCase;
import com.portafolio_manager.backend.application.port.in.project.UpdateProjectUseCase;
import com.portafolio_manager.backend.domain.project.ProjectUpdate;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    private final GetAdminProjectUseCase getAdminProjectUseCase;
    private final UpdateProjectUseCase updateProjectUseCase;

    AdminProjectController(
            ListAdminProjectsUseCase listAdminProjectsUseCase,
            CreateProjectUseCase createProjectUseCase,
            GetAdminProjectUseCase getAdminProjectUseCase,
            UpdateProjectUseCase updateProjectUseCase
    ) {
        this.listAdminProjectsUseCase = listAdminProjectsUseCase;
        this.createProjectUseCase = createProjectUseCase;
        this.getAdminProjectUseCase = getAdminProjectUseCase;
        this.updateProjectUseCase = updateProjectUseCase;
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

    @Override
    @GetMapping("/{id}")
    public AdminProjectResponse getProject(@PathVariable Long id) {
        return AdminProjectResponse.from(getAdminProjectUseCase.execute(id));
    }

    @Override
    @PutMapping("/{id}")
    public AdminProjectResponse updateProject(@PathVariable Long id, @Valid @RequestBody ProjectRequest request) {
        return AdminProjectResponse.from(updateProjectUseCase.execute(new ProjectUpdate(id, request.toDraft())));
    }
}
