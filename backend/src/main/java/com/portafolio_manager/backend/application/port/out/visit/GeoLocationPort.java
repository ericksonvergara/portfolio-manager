package com.portafolio_manager.backend.application.port.out.visit;

import java.util.Optional;

/**
 * Works out the country of an IP address without sending it anywhere.
 */
public interface GeoLocationPort {

    /** The ISO 3166-1 alpha-2 code of the country; empty when it cannot be known. */
    Optional<String> countryOf(String ip);
}
