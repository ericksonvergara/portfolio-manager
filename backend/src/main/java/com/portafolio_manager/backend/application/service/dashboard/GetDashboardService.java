package com.portafolio_manager.backend.application.service.dashboard;

import com.portafolio_manager.backend.application.port.in.dashboard.GetDashboardUseCase;
import com.portafolio_manager.backend.application.port.out.dashboard.DashboardRepositoryPort;
import com.portafolio_manager.backend.domain.dashboard.DashboardSummary;

public class GetDashboardService implements GetDashboardUseCase {

    private final DashboardRepositoryPort dashboardRepository;

    public GetDashboardService(DashboardRepositoryPort dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    @Override
    public DashboardSummary execute(Long userId) {
        return new DashboardSummary(
                dashboardRepository.countProjects(),
                dashboardRepository.countTechnologies(),
                dashboardRepository.findLatestProject().orElse(null),
                dashboardRepository.findPreviousAccess(userId).orElse(null)
        );
    }
}
