package com.portafolio_manager.backend.infrastructure.visit;

import com.maxmind.db.Reader;
import com.portafolio_manager.backend.application.port.out.visit.GeoLocationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Looks up the country in the local DB-IP Lite database (CC BY 4.0, downloaded when the Docker image is
 * built), so the visitor's IP never leaves the server. Without the file the visits are saved with no
 * country: geolocation is optional (RF021).
 */
@Component
@EnableConfigurationProperties(VisitProperties.class)
class DbIpGeoLocation implements GeoLocationPort, AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(DbIpGeoLocation.class);

    /** Only literal addresses reach InetAddress: anything else would trigger a DNS lookup. */
    private static final Pattern IP_LITERAL =
            Pattern.compile("^\\d{1,3}(\\.\\d{1,3}){3}$|^(?=.*:)[0-9a-fA-F:.]+$");

    /** DB-IP's code for addresses it has no country for. */
    private static final String NO_COUNTRY = "ZZ";

    private final Reader reader;

    DbIpGeoLocation(VisitProperties properties) {
        this.reader = open(properties.geoipDatabase());
    }

    private static Reader open(Path database) {
        if (!Files.isRegularFile(database)) {
            log.warn("IP-to-country database not found at {}: visits are saved without country", database);
            return null;
        }
        try {
            return new Reader(database.toFile());
        } catch (IOException exception) {
            log.warn("Could not open the IP-to-country database: visits are saved without country", exception);
            return null;
        }
    }

    @Override
    public Optional<String> countryOf(String ip) {
        if (reader == null || ip == null || !IP_LITERAL.matcher(ip.strip()).matches()) {
            return Optional.empty();
        }
        try {
            Map<?, ?> record = reader.get(InetAddress.getByName(ip.strip()), Map.class);
            if (record != null && record.get("country") instanceof Map<?, ?> country
                    && country.get("iso_code") instanceof String code
                    && code.length() == 2 && !NO_COUNTRY.equals(code)) {
                return Optional.of(code);
            }
        } catch (IOException exception) {
            log.warn("Could not look up the country of a visit", exception);
        }
        return Optional.empty();
    }

    @Override
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
        }
    }
}
