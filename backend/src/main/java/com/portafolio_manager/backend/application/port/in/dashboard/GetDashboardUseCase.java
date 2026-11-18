package com.portafolio_manager.backend.application.port.in.dashboard;

import com.portafolio_manager.backend.application.port.in.UseCase;
import com.portafolio_manager.backend.domain.dashboard.DashboardSummary;

/**
 * The indicators (HU012) and visit statistics (HU013) of the dashboard for the signed-in administrator,
 * given by id.
 */
public interface GetDashboardUseCase extends UseCase<Long, DashboardSummary> {
}
