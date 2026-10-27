package com.portafolio_manager.backend.domain;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Absolute http(s) URLs with a host: the only links the portfolio accepts (RN002, RN003).
 */
public final class WebUrl {

    private WebUrl() {
    }

    public static boolean isValid(String url) {
        try {
            URI uri = new URI(url);
            boolean web = "http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme());
            return web && uri.getHost() != null;
        } catch (URISyntaxException exception) {
            return false;
        }
    }
}
