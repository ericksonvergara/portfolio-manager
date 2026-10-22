package com.portafolio_manager.backend.infrastructure.web.admin.project;

import com.portafolio_manager.backend.application.port.in.project.CreateProjectUseCase;
import com.portafolio_manager.backend.application.port.in.project.DeleteProjectUseCase;
import com.portafolio_manager.backend.application.port.in.project.GetAdminProjectUseCase;
import com.portafolio_manager.backend.application.port.in.project.ListAdminProjectsUseCase;
import com.portafolio_manager.backend.application.port.in.project.SetProjectPublicationUseCase;
import com.portafolio_manager.backend.application.port.in.project.UpdateProjectUseCase;
import com.portafolio_manager.backend.domain.project.ProjectPublication;
import com.portafolio_manager.backend.domain.project.ProjectUpdate;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
    private final DeleteProjectUseCase deleteProjectUseCase;
    private final SetProjectPublicationUseCase setProjectPublicationUseCase;

    AdminProjectController(
            ListAdminProjectsUseCase listAdminProjectsUseCase,
            CreateProjectUseCase createProjectUseCase,
            GetAdminProjectUseCase getAdminProjectUseCase,
            UpdateProjectUseCase updateProjectUseCase,
            DeleteProjectUseCase deleteProjectUseCase,
            SetProjectPublicationUseCase setProjectPublicationUseCase
    ) {
        this.listAdminProjectsUseCase = listAdminProjectsUseCase;
        this.createProjectUseCase = createProjectUseCase;
        this.getAdminProjectUseCase = getAdminProjectUseCase;
        this.updateProjectUseCase = updateProjectUseCase;
        this.deleteProjectUseCase = deleteProjectUseCase;
        this.setProjectPublicationUseCase = setProjectPublicationUseCase;
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

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        deleteProjectUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/{id}/publication")
    public AdminProjectResponse setPublication(@PathVariable Long id, @Valid @RequestBody PublicationRequest request) {
        return AdminProjectResponse.from(
                setProjectPublicationUseCase.execute(new ProjectPublication(id, request.active())));
    }
}
