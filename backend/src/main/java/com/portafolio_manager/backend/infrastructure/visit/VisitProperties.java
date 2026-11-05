package com.portafolio_manager.backend.infrastructure.visit;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Visit registration settings (HU025): where the IP-to-country database lives.
 */
@ConfigurationProperties(prefix = "app.visits")
public record VisitProperties(Path geoipDatabase) {

    public VisitProperties {
        Objects.requireNonNull(geoipDatabase, "app.visits.geoip-database must not be null");
    }
}
