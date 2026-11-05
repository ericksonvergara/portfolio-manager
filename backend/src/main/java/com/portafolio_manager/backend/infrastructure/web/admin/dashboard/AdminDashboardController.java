package com.portafolio_manager.backend.infrastructure.web.admin.dashboard;

import com.portafolio_manager.backend.application.port.in.dashboard.GetDashboardUseCase;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
class AdminDashboardController implements AdminDashboardApi {

    private final GetDashboardUseCase getDashboardUseCase;

    AdminDashboardController(GetDashboardUseCase getDashboardUseCase) {
        this.getDashboardUseCase = getDashboardUseCase;
    }

    @Override
    @GetMapping
    public DashboardResponse getDashboard(@AuthenticationPrincipal Jwt jwt) {
        return DashboardResponse.from(getDashboardUseCase.execute(Long.valueOf(jwt.getSubject())));
    }
}
