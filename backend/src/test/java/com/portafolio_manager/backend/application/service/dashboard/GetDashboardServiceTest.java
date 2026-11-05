package com.portafolio_manager.backend.application.service.dashboard;

import com.portafolio_manager.backend.application.port.out.dashboard.DashboardRepositoryPort;
import com.portafolio_manager.backend.domain.dashboard.DashboardSummary;
import com.portafolio_manager.backend.domain.dashboard.LatestProject;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetDashboardServiceTest {

    private final DashboardRepositoryPort repository = mock(DashboardRepositoryPort.class);
    private final GetDashboardService service = new GetDashboardService(repository);

    @Test
    void shouldGatherTheIndicators() {
        LatestProject latest = new LatestProject("Sistema ERP", Instant.parse("2026-11-02T15:30:00Z"));
        Instant access = Instant.parse("2026-11-04T21:15:00Z");
        when(repository.countProjects()).thenReturn(12L);
        when(repository.countTechnologies()).thenReturn(24L);
        when(repository.findLatestProject()).thenReturn(Optional.of(latest));
        when(repository.findPreviousAccess(7L)).thenReturn(Optional.of(access));

        assertThat(service.execute(7L)).isEqualTo(new DashboardSummary(12, 24, latest, access));
    }

    @Test
    void shouldWorkOnAnEmptyPortfolioAndAFirstSession() {
        when(repository.findLatestProject()).thenReturn(Optional.empty());
        when(repository.findPreviousAccess(7L)).thenReturn(Optional.empty());

        assertThat(service.execute(7L)).isEqualTo(new DashboardSummary(0, 0, null, null));
    }
}
