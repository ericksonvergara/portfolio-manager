package com.portafolio_manager.backend.infrastructure.web.admin.technology;

import com.portafolio_manager.backend.application.port.in.technology.CreateTechnologyUseCase;
import com.portafolio_manager.backend.application.port.in.technology.DeleteTechnologyUseCase;
import com.portafolio_manager.backend.application.port.in.technology.ListAdminTechnologiesUseCase;
import com.portafolio_manager.backend.application.port.in.technology.UpdateTechnologyUseCase;
import com.portafolio_manager.backend.domain.technology.TechnologyUpdate;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/admin/technologies")
class AdminTechnologyController implements AdminTechnologyApi {

    private final ListAdminTechnologiesUseCase listAdminTechnologiesUseCase;
    private final CreateTechnologyUseCase createTechnologyUseCase;
    private final UpdateTechnologyUseCase updateTechnologyUseCase;
    private final DeleteTechnologyUseCase deleteTechnologyUseCase;

    AdminTechnologyController(
            ListAdminTechnologiesUseCase listAdminTechnologiesUseCase,
            CreateTechnologyUseCase createTechnologyUseCase,
            UpdateTechnologyUseCase updateTechnologyUseCase,
            DeleteTechnologyUseCase deleteTechnologyUseCase
    ) {
        this.listAdminTechnologiesUseCase = listAdminTechnologiesUseCase;
        this.createTechnologyUseCase = createTechnologyUseCase;
        this.updateTechnologyUseCase = updateTechnologyUseCase;
        this.deleteTechnologyUseCase = deleteTechnologyUseCase;
    }

    @Override
    @GetMapping
    public List<AdminTechnologyResponse> listTechnologies() {
        return listAdminTechnologiesUseCase.execute(null).stream().map(AdminTechnologyResponse::from).toList();
    }

    @Override
    @PostMapping
    public ResponseEntity<AdminTechnologyResponse> createTechnology(@Valid @RequestBody TechnologyRequest request) {
        AdminTechnologyResponse created =
                AdminTechnologyResponse.from(createTechnologyUseCase.execute(request.toDraft()));
        return ResponseEntity.created(URI.create("/api/admin/technologies/" + created.id())).body(created);
    }

    @Override
    @PutMapping("/{id}")
    public AdminTechnologyResponse updateTechnology(
            @PathVariable Long id,
            @Valid @RequestBody TechnologyRequest request
    ) {
        return AdminTechnologyResponse.from(
                updateTechnologyUseCase.execute(new TechnologyUpdate(id, request.toDraft())));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTechnology(@PathVariable Long id) {
        deleteTechnologyUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
