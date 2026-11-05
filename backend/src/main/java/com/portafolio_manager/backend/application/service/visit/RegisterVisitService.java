package com.portafolio_manager.backend.application.service.visit;

import com.portafolio_manager.backend.application.port.in.visit.RegisterVisitUseCase;
import com.portafolio_manager.backend.application.port.out.project.ProjectRepositoryPort;
import com.portafolio_manager.backend.application.port.out.visit.GeoLocationPort;
import com.portafolio_manager.backend.application.port.out.visit.VisitRepositoryPort;
import com.portafolio_manager.backend.domain.project.ProjectNotFoundException;
import com.portafolio_manager.backend.domain.visit.AnonymizedIp;
import com.portafolio_manager.backend.domain.visit.Visit;
import com.portafolio_manager.backend.domain.visit.VisitRequest;
import com.portafolio_manager.backend.domain.visit.VisitorAgent;

import java.time.Clock;

public class RegisterVisitService implements RegisterVisitUseCase {

    private final VisitRepositoryPort visitRepository;
    private final ProjectRepositoryPort projectRepository;
    private final GeoLocationPort geoLocation;
    private final Clock clock;

    public RegisterVisitService(
            VisitRepositoryPort visitRepository,
            ProjectRepositoryPort projectRepository,
            GeoLocationPort geoLocation,
            Clock clock
    ) {
        this.visitRepository = visitRepository;
        this.projectRepository = projectRepository;
        this.geoLocation = geoLocation;
        this.clock = clock;
    }

    @Override
    public Void execute(VisitRequest request) {
        Long projectId = null;
        if (request.projectSlug() != null) {
            projectId = projectRepository.findPublishedIdBySlug(request.projectSlug())
                    .orElseThrow(() -> ProjectNotFoundException.withSlug(request.projectSlug()));
        }

        // The full IP only reaches the geolocation: what is stored is the masked one.
        visitRepository.save(new Visit(
                clock.instant(),
                AnonymizedIp.of(request.clientIp()),
                VisitorAgent.parse(request.userAgent()),
                geoLocation.countryOf(request.clientIp()).orElse(null),
                projectId
        ));
        return null;
    }
}
