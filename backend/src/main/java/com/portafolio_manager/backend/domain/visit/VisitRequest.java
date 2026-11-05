package com.portafolio_manager.backend.domain.visit;

/**
 * What the site knows when a page is opened: the client IP and User-Agent, and the slug of the project
 * when the page is its detail. The IP is only used to work out the country and to be masked.
 */
public record VisitRequest(String clientIp, String userAgent, String projectSlug) {
}
