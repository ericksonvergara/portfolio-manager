package com.portafolio_manager.backend.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.ZoneId;
import java.util.Objects;

/**
 * Dashboard statistics settings (HU013): the time zone of "today" and "this month". An unknown zone stops
 * the start-up, so the figures can never be silently counted in the wrong one.
 */
@ConfigurationProperties(prefix = "app.statistics")
public record StatisticsProperties(ZoneId timezone) {

    public StatisticsProperties {
        Objects.requireNonNull(timezone, "app.statistics.timezone must not be null");
    }
}
