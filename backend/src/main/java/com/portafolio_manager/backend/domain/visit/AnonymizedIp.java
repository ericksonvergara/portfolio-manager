package com.portafolio_manager.backend.domain.visit;

import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Masks a client IP so it no longer identifies a person (RF021): an IPv4 address keeps its first three
 * blocks and an IPv6 address its first three groups, which is the network and not the visitor.
 */
public final class AnonymizedIp {

    public static final String UNKNOWN = "unknown";

    private static final Pattern IPV4 = Pattern.compile("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$");
    private static final Pattern IPV6 = Pattern.compile("^[0-9a-fA-F:]+$");
    private static final String IPV4_MAPPED_PREFIX = "::ffff:";
    private static final int IPV6_KEPT_GROUPS = 3;

    private AnonymizedIp() {
    }

    public static String of(String ip) {
        if (ip == null) {
            return UNKNOWN;
        }
        String address = ip.strip();
        if (address.toLowerCase(Locale.ROOT).startsWith(IPV4_MAPPED_PREFIX)) {
            address = address.substring(IPV4_MAPPED_PREFIX.length());
        }
        var ipv4 = IPV4.matcher(address);
        if (ipv4.matches()) {
            return ipv4.group(1) + "." + ipv4.group(2) + "." + ipv4.group(3) + ".0";
        }
        if (address.contains(":") && IPV6.matcher(address).matches()) {
            return maskIpv6(address);
        }
        return UNKNOWN;
    }

    private static String maskIpv6(String address) {
        // Text before "::" holds the leading groups; what follows it would be the host part anyway.
        String network = Arrays.stream(address.split("::", 2)[0].split(":"))
                .filter(group -> !group.isEmpty())
                .limit(IPV6_KEPT_GROUPS)
                .map(group -> group.toLowerCase(Locale.ROOT))
                .collect(Collectors.joining(":"));
        return network + "::";
    }
}
