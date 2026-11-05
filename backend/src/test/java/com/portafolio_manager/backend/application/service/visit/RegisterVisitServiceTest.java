package com.portafolio_manager.backend.application.service.visit;

import com.portafolio_manager.backend.application.port.out.project.ProjectRepositoryPort;
import com.portafolio_manager.backend.application.port.out.visit.GeoLocationPort;
import com.portafolio_manager.backend.application.port.out.visit.VisitDeduplicatorPort;
import com.portafolio_manager.backend.application.port.out.visit.VisitRepositoryPort;
import com.portafolio_manager.backend.domain.project.ProjectNotFoundException;
import com.portafolio_manager.backend.domain.visit.Visit;
import com.portafolio_manager.backend.domain.visit.VisitRequest;
import com.portafolio_manager.backend.domain.visit.VisitorAgent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RegisterVisitServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-05T15:30:00Z");
    private static final String CHROME_ON_WINDOWS =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/130.0 Safari/537.36";
    private static final VisitorAgent AGENT = new VisitorAgent("Chrome", "Windows", "Desktop");

    private final VisitRepositoryPort visitRepository = mock(VisitRepositoryPort.class);
    private final ProjectRepositoryPort projectRepository = mock(ProjectRepositoryPort.class);
    private final GeoLocationPort geoLocation = mock(GeoLocationPort.class);
    private final VisitDeduplicatorPort deduplicator = mock(VisitDeduplicatorPort.class);
    private final RegisterVisitService service = new RegisterVisitService(
            visitRepository, projectRepository, geoLocation, deduplicator, Clock.fixed(NOW, ZoneOffset.UTC));

    @BeforeEach
    void everyVisitIsNewByDefault() {
        when(deduplicator.isFirstInWindow(any(VisitRequest.class))).thenReturn(true);
    }

    @Test
    void shouldSaveAVisitToTheLandingPageWithTheIpMasked() {
        when(geoLocation.countryOf("190.25.14.77")).thenReturn(Optional.of("CO"));

        service.execute(new VisitRequest("190.25.14.77", CHROME_ON_WINDOWS, null));

        verify(visitRepository).save(new Visit(NOW, "190.25.14.0", AGENT, "CO", null));
        verifyNoInteractions(projectRepository);
    }

    @Test
    void shouldRelateTheVisitWithItsProject() {
        when(geoLocation.countryOf("190.25.14.77")).thenReturn(Optional.empty());
        when(projectRepository.findPublishedIdBySlug("sistema-erp")).thenReturn(Optional.of(7L));

        service.execute(new VisitRequest("190.25.14.77", CHROME_ON_WINDOWS, "sistema-erp"));

        verify(visitRepository).save(new Visit(NOW, "190.25.14.0", AGENT, null, 7L));
    }

    @Test
    void shouldNotSaveAVisitToAProjectThatIsNotPublished() {
        when(projectRepository.findPublishedIdBySlug("oculto")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new VisitRequest("190.25.14.77", CHROME_ON_WINDOWS, "oculto")))
                .isInstanceOf(ProjectNotFoundException.class);
        verifyNoInteractions(visitRepository, deduplicator);
    }

    @Test
    void shouldNotSaveAVisitThatRepeatsWithinTheWindow() {
        when(deduplicator.isFirstInWindow(any(VisitRequest.class))).thenReturn(false);

        service.execute(new VisitRequest("190.25.14.77", CHROME_ON_WINDOWS, null));

        verifyNoInteractions(visitRepository, geoLocation);
    }
}
