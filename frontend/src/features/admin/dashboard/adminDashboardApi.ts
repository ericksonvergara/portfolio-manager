import { api } from '../../../services/api'

/** The general indicators of the portfolio (HU012). */
export interface DashboardSummary {
  totalProjects: number
  totalTechnologies: number
  /** The last project registered, published or not; null while there is none. */
  latestProject: { name: string; registeredAt: string } | null
  /** The access before the current session; null on the first one. */
  lastAccessAt: string | null
}

export async function getDashboard(): Promise<DashboardSummary> {
  const { data } = await api.get<DashboardSummary>('/api/admin/dashboard')
  return data
}
