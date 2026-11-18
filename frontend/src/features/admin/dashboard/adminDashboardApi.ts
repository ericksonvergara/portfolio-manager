import { api } from '../../../services/api'

/** Visits to the public site, without reloads (HU013). */
export interface VisitStatistics {
  total: number
  /** Today and this month follow the time zone of the backend (STATS_TIMEZONE). */
  today: number
  thisMonth: number
  /** Null while no project detail has visits. */
  mostVisitedProject: { name: string; visits: number } | null
}

/** The indicators of the portfolio (HU012) and its visits (HU013). */
export interface DashboardSummary {
  totalProjects: number
  totalTechnologies: number
  /** The last project registered, published or not; null while there is none. */
  latestProject: { name: string; registeredAt: string } | null
  /** The last one published among those still published; null while there is none. */
  latestPublishedProject: { name: string; publishedAt: string } | null
  /** The access before the current session; null on the first one. */
  lastAccessAt: string | null
  visits: VisitStatistics
}

export async function getDashboard(): Promise<DashboardSummary> {
  const { data } = await api.get<DashboardSummary>('/api/admin/dashboard')
  return data
}
