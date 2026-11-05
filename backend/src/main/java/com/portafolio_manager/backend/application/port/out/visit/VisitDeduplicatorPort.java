package com.portafolio_manager.backend.application.port.out.visit;

import com.portafolio_manager.backend.domain.visit.VisitRequest;

/**
 * Tells a new visit from a repeated one, such as a reload of the same page (HU027).
 */
public interface VisitDeduplicatorPort {

    /** True the first time this visitor opens this page within the time window. */
    boolean isFirstInWindow(VisitRequest request);
}
