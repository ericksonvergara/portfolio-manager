package com.portafolio_manager.backend.application.service.dashboard;

import com.portafolio_manager.backend.application.port.out.dashboard.DashboardRepositoryPort;
import com.portafolio_manager.backend.domain.dashboard.DashboardSummary;
import com.portafolio_manager.backend.domain.dashboard.LatestProject;
import com.portafolio_manager.backend.domain.dashboard.LatestPublishedProject;
import com.portafolio_manager.backend.domain.dashboard.RankedProject;
import com.portafolio_manager.backend.domain.dashboard.VisitStatistics;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetDashboardServiceTest {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    private final DashboardRepositoryPort repository = mock(DashboardRepositoryPort.class);

    @Test
    void shouldGatherTheIndicators() {
        LatestProject latest = new LatestProject("Sistema ERP", Instant.parse("2026-11-02T15:30:00Z"));
        LatestPublishedProject published = new LatestPublishedProject("Tienda", Instant.parse("2026-11-03T10:00:00Z"));
        List<RankedProject> ranking = List.of(
                new RankedProject(7L, "Sistema ERP", 320, true), new RankedProject(9L, "Tienda", 80, false));
        Instant access = Instant.parse("2026-11-04T21:15:00Z");
        when(repository.countProjects()).thenReturn(12L);
        when(repository.countTechnologies()).thenReturn(24L);
        when(repository.findLatestProject()).thenReturn(Optional.of(latest));
        when(repository.findLatestPublishedProject()).thenReturn(Optional.of(published));
        when(repository.findPreviousAccess(7L)).thenReturn(Optional.of(access));
        when(repository.countVisits()).thenReturn(1500L);
        when(repository.countVisitsSince(Instant.parse("2026-11-05T05:00:00Z"))).thenReturn(54L);
        when(repository.countVisitsSince(Instant.parse("2026-11-01T05:00:00Z"))).thenReturn(812L);
        when(repository.findMostVisitedProjects(GetDashboardService.RANKING_SIZE)).thenReturn(ranking);

        assertThat(serviceAt("2026-11-05T15:00:00Z").execute(7L)).isEqualTo(new DashboardSummary(
                12, 24, latest, published, access, new VisitStatistics(1500, 54, 812, ranking)));
    }

    @Test
    void shouldStartTheDayAndMonthInTheConfiguredTimeZone() {
        // 03:00 UTC on December 1st is still 22:00 on November 30th in Bogotá.
        when(repository.findLatestProject()).thenReturn(Optional.empty());
        when(repository.findLatestPublishedProject()).thenReturn(Optional.empty());
        when(repository.findPreviousAccess(7L)).thenReturn(Optional.empty());
        when(repository.countVisitsSince(Instant.parse("2026-11-30T05:00:00Z"))).thenReturn(5L);
        when(repository.countVisitsSince(Instant.parse("2026-11-01T05:00:00Z"))).thenReturn(90L);

        VisitStatistics visits = serviceAt("2026-12-01T03:00:00Z").execute(7L).visits();

        assertThat(visits.today()).isEqualTo(5);
        assertThat(visits.thisMonth()).isEqualTo(90);
    }

    @Test
    void shouldWorkOnAnEmptyPortfolioAndAFirstSession() {
        when(repository.findLatestProject()).thenReturn(Optional.empty());
        when(repository.findLatestPublishedProject()).thenReturn(Optional.empty());
        when(repository.findPreviousAccess(7L)).thenReturn(Optional.empty());

        assertThat(serviceAt("2026-11-05T15:00:00Z").execute(7L)).isEqualTo(
                new DashboardSummary(0, 0, null, null, null, new VisitStatistics(0, 0, 0, List.of())));
    }

    private GetDashboardService serviceAt(String instant) {
        return new GetDashboardService(repository, Clock.fixed(Instant.parse(instant), ZoneOffset.UTC), BOGOTA);
    }

    @Test
    void shouldTakeTheMostVisitedProjectFromTheTopOfTheRanking() {
        VisitStatistics visits = new VisitStatistics(10, 1, 5, List.of(
                new RankedProject(7L, "Sistema ERP", 6, true), new RankedProject(9L, "Tienda", 4, true)));

        assertThat(visits.mostVisitedProject()).contains(new RankedProject(7L, "Sistema ERP", 6, true));
        assertThat(new VisitStatistics(0, 0, 0, List.of()).mostVisitedProject()).isEmpty();
    }
}
