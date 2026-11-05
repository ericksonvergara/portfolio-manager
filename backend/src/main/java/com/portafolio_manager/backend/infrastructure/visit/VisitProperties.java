package com.portafolio_manager.backend.infrastructure.visit;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;

/**
 * Visit registration settings: where the IP-to-country database lives (HU025), and for how long reopening
 * the same page does not count as a new visit (HU027).
 */
@ConfigurationProperties(prefix = "app.visits")
public record VisitProperties(Path geoipDatabase, Duration dedupWindow) {

    public VisitProperties {
        Objects.requireNonNull(geoipDatabase, "app.visits.geoip-database must not be null");
        Objects.requireNonNull(dedupWindow, "app.visits.dedup-window must not be null");
        if (dedupWindow.isZero() || dedupWindow.isNegative()) {
            throw new IllegalArgumentException("app.visits.dedup-window (VISIT_DEDUP_WINDOW) must be positive");
        }
    }
}
