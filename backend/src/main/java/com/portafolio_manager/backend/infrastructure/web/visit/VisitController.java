package com.portafolio_manager.backend.infrastructure.web.visit;

import com.portafolio_manager.backend.application.port.in.visit.RegisterVisitUseCase;
import com.portafolio_manager.backend.domain.visit.VisitRequest;
import com.portafolio_manager.backend.infrastructure.web.ClientIp;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/visits")
class VisitController implements VisitApi {

    private final RegisterVisitUseCase registerVisitUseCase;

    VisitController(RegisterVisitUseCase registerVisitUseCase) {
        this.registerVisitUseCase = registerVisitUseCase;
    }

    @Override
    @PostMapping
    public ResponseEntity<Void> registerVisit(
            @Valid @RequestBody(required = false) VisitBody body,
            @RequestHeader(name = HttpHeaders.USER_AGENT, required = false) String userAgent,
            HttpServletRequest request
    ) {
        String projectSlug = body == null || body.projectSlug() == null || body.projectSlug().isBlank()
                ? null
                : body.projectSlug().strip();
        registerVisitUseCase.execute(new VisitRequest(ClientIp.of(request), userAgent, projectSlug));
        return ResponseEntity.accepted().build();
    }
}
