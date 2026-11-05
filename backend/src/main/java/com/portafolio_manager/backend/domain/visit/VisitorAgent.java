package com.portafolio_manager.backend.domain.visit;

import java.util.Locale;

/**
 * The browser, operating system and kind of device read from a User-Agent header (HU025). Only these
 * three names are kept, never the header itself.
 */
public record VisitorAgent(String browser, String operatingSystem, String device) {

    public static final String UNKNOWN = "Unknown";

    public static VisitorAgent parse(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return new VisitorAgent(UNKNOWN, UNKNOWN, UNKNOWN);
        }
        String agent = userAgent.toLowerCase(Locale.ROOT);
        return new VisitorAgent(browser(agent), operatingSystem(agent), device(agent));
    }

    /** Order matters: Edge and Opera also say "Chrome", and Chrome also says "Safari". */
    private static String browser(String agent) {
        if (isBot(agent)) {
            return "Bot";
        }
        if (agent.contains("edg/") || agent.contains("edga/") || agent.contains("edgios/")) {
            return "Edge";
        }
        if (agent.contains("opr/") || agent.contains("opera")) {
            return "Opera";
        }
        if (agent.contains("samsungbrowser")) {
            return "Samsung Internet";
        }
        if (agent.contains("firefox/") || agent.contains("fxios/")) {
            return "Firefox";
        }
        if (agent.contains("chrome/") || agent.contains("crios/")) {
            return "Chrome";
        }
        if (agent.contains("safari/")) {
            return "Safari";
        }
        return UNKNOWN;
    }

    /** Order matters: Android says "Linux", and iOS says "like Mac OS X". */
    private static String operatingSystem(String agent) {
        if (agent.contains("windows")) {
            return "Windows";
        }
        if (agent.contains("android")) {
            return "Android";
        }
        if (agent.contains("iphone") || agent.contains("ipad") || agent.contains("ipod")) {
            return "iOS";
        }
        if (agent.contains("mac os x") || agent.contains("macintosh")) {
            return "macOS";
        }
        if (agent.contains("cros")) {
            return "ChromeOS";
        }
        if (agent.contains("linux")) {
            return "Linux";
        }
        return UNKNOWN;
    }

    private static String device(String agent) {
        if (isBot(agent)) {
            return "Bot";
        }
        if (agent.contains("ipad") || agent.contains("tablet")
                || (agent.contains("android") && !agent.contains("mobile"))) {
            return "Tablet";
        }
        if (agent.contains("mobile") || agent.contains("iphone") || agent.contains("ipod")) {
            return "Mobile";
        }
        return "Desktop";
    }

    private static boolean isBot(String agent) {
        return agent.contains("bot") || agent.contains("crawler") || agent.contains("spider")
                || agent.contains("slurp");
    }
}
