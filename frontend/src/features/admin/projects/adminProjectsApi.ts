import { api } from '../../../services/api'
import type { ProjectStatus } from '../../landing/landingApi'

/** A project as the admin panel sees it, active or not. */
export interface AdminProject {
  id: number
  slug: string
  name: string
  shortDescription: string
  description: string
  coverImageUrl: string | null
  projectUrl: string | null
  repositoryUrl: string | null
  videoUrl: string | null
  status: ProjectStatus
  /** ISO date (YYYY-MM-DD), or null when unknown. */
  developedOn: string | null
  displayOrder: number
  featured: boolean
  active: boolean
  technologies: { id: number; name: string }[]
}

/** What the project form sends (HU015). Images are managed apart (HU019). */
export interface ProjectInput {
  name: string
  shortDescription: string
  description: string
  projectUrl: string | null
  repositoryUrl: string | null
  videoUrl: string | null
  status: ProjectStatus
  developedOn: string | null
  displayOrder: number
  featured: boolean
  technologyIds: number[]
}

export async function listAdminProjects(): Promise<AdminProject[]> {
  const { data } = await api.get<AdminProject[]>('/api/admin/projects')
  return data
}

export async function createProject(
  input: ProjectInput,
): Promise<AdminProject> {
  const { data } = await api.post<AdminProject>('/api/admin/projects', input)
  return data
}
