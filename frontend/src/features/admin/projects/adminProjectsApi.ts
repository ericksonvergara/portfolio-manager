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
  /** Gallery in display order (HU019). */
  gallery: AdminGalleryImage[]
}

export interface AdminGalleryImage {
  id: number
  url: string
  altText: string | null
}

/** What the project form sends (HU015, HU016). Images are managed apart (HU019). */
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

export async function getAdminProject(id: number): Promise<AdminProject> {
  const { data } = await api.get<AdminProject>(`/api/admin/projects/${id}`)
  return data
}

export async function updateProject(
  id: number,
  input: ProjectInput,
): Promise<AdminProject> {
  const { data } = await api.put<AdminProject>(
    `/api/admin/projects/${id}`,
    input,
  )
  return data
}

export async function deleteProject(id: number): Promise<void> {
  await api.delete(`/api/admin/projects/${id}`)
}

/** Publishes or hides a project (HU018); a project needs a cover image to be published (RN004). */
export async function setProjectPublication(
  id: number,
  active: boolean,
): Promise<AdminProject> {
  const { data } = await api.patch<AdminProject>(
    `/api/admin/projects/${id}/publication`,
    { active },
  )
  return data
}

/* Images (HU019). The default JSON content type would turn FormData into JSON, so these requests
 * ask for multipart and let the browser add the boundary. */
const MULTIPART = { headers: { 'Content-Type': 'multipart/form-data' } }

function imageForm(file: File, altText?: string): FormData {
  const form = new FormData()
  form.append('file', file)
  if (altText?.trim()) form.append('altText', altText.trim())
  return form
}

export async function uploadCover(
  id: number,
  file: File,
): Promise<AdminProject> {
  const { data } = await api.put<AdminProject>(
    `/api/admin/projects/${id}/cover`,
    imageForm(file),
    MULTIPART,
  )
  return data
}

export async function removeCover(id: number): Promise<AdminProject> {
  const { data } = await api.delete<AdminProject>(
    `/api/admin/projects/${id}/cover`,
  )
  return data
}

export async function addGalleryImage(
  id: number,
  file: File,
  altText: string,
): Promise<AdminProject> {
  const { data } = await api.post<AdminProject>(
    `/api/admin/projects/${id}/images`,
    imageForm(file, altText),
    MULTIPART,
  )
  return data
}

export async function removeGalleryImage(
  id: number,
  imageId: number,
): Promise<AdminProject> {
  const { data } = await api.delete<AdminProject>(
    `/api/admin/projects/${id}/images/${imageId}`,
  )
  return data
}
