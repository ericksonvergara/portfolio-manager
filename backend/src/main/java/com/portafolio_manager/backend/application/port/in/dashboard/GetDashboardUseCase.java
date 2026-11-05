package com.portafolio_manager.backend.application.port.in.dashboard;

import com.portafolio_manager.backend.application.port.in.UseCase;
import com.portafolio_manager.backend.domain.dashboard.DashboardSummary;

/**
 * The indicators of the dashboard for the signed-in administrator, given by id (HU012).
 */
public interface GetDashboardUseCase extends UseCase<Long, DashboardSummary> {
}
