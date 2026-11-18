package com.portafolio_manager.backend.domain.dashboard;

/**
 * The visits to the public site (HU013). The most visited project is null while no detail has visits.
 */
public record VisitStatistics(long total, long today, long thisMonth, MostVisitedProject mostVisitedProject) {
}
