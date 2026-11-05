import { api } from '../../services/api'

/** Tells the backend a public page was opened (HU025); with a slug, the detail of that project. */
export async function registerVisit(projectSlug?: string): Promise<void> {
  await api.post('/api/public/visits', { projectSlug: projectSlug ?? null })
}
