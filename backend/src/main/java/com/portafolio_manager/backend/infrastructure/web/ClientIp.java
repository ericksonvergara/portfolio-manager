package com.portafolio_manager.backend.infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;

/**
 * The address of the visitor. Set by nginx in X-Real-IP, which overwrites any value sent by the client;
 * the backend is only reachable through it.
 */
public final class ClientIp {

    private static final String REAL_IP_HEADER = "X-Real-IP";

    private ClientIp() {
    }

    public static String of(HttpServletRequest request) {
        String realIp = request.getHeader(REAL_IP_HEADER);
        return realIp == null || realIp.isBlank() ? request.getRemoteAddr() : realIp.strip();
    }
}
