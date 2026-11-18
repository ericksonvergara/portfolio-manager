package com.portafolio_manager.backend.application.service.dashboard;

import com.portafolio_manager.backend.application.port.in.dashboard.GetDashboardUseCase;
import com.portafolio_manager.backend.application.port.out.dashboard.DashboardRepositoryPort;
import com.portafolio_manager.backend.domain.dashboard.DashboardSummary;
import com.portafolio_manager.backend.domain.dashboard.VisitStatistics;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

public class GetDashboardService implements GetDashboardUseCase {

    /** Projects in the ranking (HU014). */
    static final int RANKING_SIZE = 5;

    private final DashboardRepositoryPort dashboardRepository;
    private final Clock clock;
    private final ZoneId timezone;

    public GetDashboardService(DashboardRepositoryPort dashboardRepository, Clock clock, ZoneId timezone) {
        this.dashboardRepository = dashboardRepository;
        this.clock = clock;
        this.timezone = timezone;
    }

    @Override
    public DashboardSummary execute(Long userId) {
        return new DashboardSummary(
                dashboardRepository.countProjects(),
                dashboardRepository.countTechnologies(),
                dashboardRepository.findLatestProject().orElse(null),
                dashboardRepository.findLatestPublishedProject().orElse(null),
                dashboardRepository.findPreviousAccess(userId).orElse(null),
                visits()
        );
    }

    /** "Today" and "this month" start at midnight in the time zone of the statistics, not of the server. */
    private VisitStatistics visits() {
        LocalDate today = LocalDate.ofInstant(clock.instant(), timezone);
        return new VisitStatistics(
                dashboardRepository.countVisits(),
                dashboardRepository.countVisitsSince(today.atStartOfDay(timezone).toInstant()),
                dashboardRepository.countVisitsSince(today.withDayOfMonth(1).atStartOfDay(timezone).toInstant()),
                dashboardRepository.findMostVisitedProjects(RANKING_SIZE)
        );
    }
}
