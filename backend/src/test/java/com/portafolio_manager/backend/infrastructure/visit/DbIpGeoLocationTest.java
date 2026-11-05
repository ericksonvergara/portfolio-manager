package com.portafolio_manager.backend.infrastructure.visit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The real database is downloaded when the Docker image is built, so it is not available here: these
 * tests cover the fallbacks. The lookup itself was checked by hand against the running stack.
 */
class DbIpGeoLocationTest {

    @Test
    void shouldWorkWithoutTheDatabase(@TempDir Path directory) throws IOException {
        try (DbIpGeoLocation geoLocation = new DbIpGeoLocation(new VisitProperties(directory.resolve("none.mmdb")))) {
            assertThat(geoLocation.countryOf("8.8.8.8")).isEmpty();
        }
    }

    @Test
    void shouldWorkWithADamagedDatabase(@TempDir Path directory) throws IOException {
        Path damaged = Files.writeString(directory.resolve("damaged.mmdb"), "this is not a database");

        try (DbIpGeoLocation geoLocation = new DbIpGeoLocation(new VisitProperties(damaged))) {
            assertThat(geoLocation.countryOf("8.8.8.8")).isEmpty();
        }
    }
}
