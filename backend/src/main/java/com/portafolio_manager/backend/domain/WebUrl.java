package com.portafolio_manager.backend.domain;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * The links the portfolio accepts (RN002, RN003): absolute http(s) URLs that a visitor can open, so the
 * host must be a domain with a dot (or an IP address), the port must exist, and no user or password may
 * be embedded ("https://github.com@evil.example" is a classic phishing trick). Hosts with non-ASCII
 * letters (for example "ñ") must be written in their xn-- form.
 */
public final class WebUrl {

    private static final int MAX_PORT = 65_535;

    private WebUrl() {
    }

    public static boolean isValid(String url) {
        try {
            URI uri = new URI(url);
            boolean web = "http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme());
            String host = uri.getHost();
            return web
                    && host != null
                    && (host.contains(".") || host.startsWith("["))
                    && uri.getRawUserInfo() == null
                    && uri.getPort() <= MAX_PORT;
        } catch (URISyntaxException exception) {
            return false;
        }
    }
}
