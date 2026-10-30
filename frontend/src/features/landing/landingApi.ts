import { api } from '../../services/api'

export type SocialPlatform =
  'GITHUB' | 'LINKEDIN' | 'X' | 'INSTAGRAM' | 'YOUTUBE' | 'WEBSITE'

export type TechnologyCategory =
  'BACKEND' | 'FRONTEND' | 'DATABASE' | 'DEVOPS' | 'MOBILE' | 'AI' | 'TOOLS'

/** The kinds of project a visitor can filter by, derived from their technologies (HU005). */
export type ProjectCategory =
  'BACKEND' | 'FRONTEND' | 'FULL_STACK' | 'MOBILE' | 'DEVOPS' | 'AI'

export interface SocialLink {
  platform: SocialPlatform
  url: string
}

export interface Profile {
  fullName: string
  headline: string
  about: string
  photoUrl: string | null
  email: string
  location: string | null
  socialLinks: SocialLink[]
}

export interface Technology {
  id: number
  name: string
  category: TechnologyCategory
}

/** A published project as the landing page lists it (HU002). */
export interface ProjectCard {
  slug: string
  name: string
  shortDescription: string
  coverImageUrl: string
  projectUrl: string | null
  repositoryUrl: string | null
  featured: boolean
  technologies: string[]
}

export type ProjectStatus = 'IN_PROGRESS' | 'COMPLETED'

export interface GalleryImage {
  url: string
  altText: string | null
}

/** Everything the project detail page shows (HU003). */
export interface ProjectDetail {
  slug: string
  name: string
  description: string
  coverImageUrl: string
  projectUrl: string | null
  repositoryUrl: string | null
  videoUrl: string | null
  status: ProjectStatus
  /** ISO date (YYYY-MM-DD), or null when unknown. */
  developedOn: string | null
  gallery: GalleryImage[]
  technologies: string[]
}

export async function getProfile(): Promise<Profile> {
  const { data } = await api.get<Profile>('/api/public/profile')
  return data
}

export async function getTechnologies(): Promise<Technology[]> {
  const { data } = await api.get<Technology[]>('/api/public/technologies')
  return data
}

export interface ProjectFilter {
  /** Name or technology (HU004). */
  search?: string
  /** Kind of project (HU005). */
  category?: ProjectCategory | null
}

/** The published projects, optionally narrowed by a search and a category; both can be combined. */
export async function getProjects(
  filter: ProjectFilter = {},
): Promise<ProjectCard[]> {
  const search = filter.search?.trim()
  const params = {
    ...(search ? { search } : {}),
    ...(filter.category ? { category: filter.category } : {}),
  }
  const { data } = await api.get<ProjectCard[]>('/api/public/projects', {
    params,
  })
  return data
}

export async function getProject(slug: string): Promise<ProjectDetail> {
  const { data } = await api.get<ProjectDetail>(
    `/api/public/projects/${encodeURIComponent(slug)}`,
  )
  return data
}

/** What a visitor writes in the contact form (HU006). */
export interface ContactInput {
  name: string
  email: string
  message: string
  /** Hidden field that only bots fill in: the backend discards their message. */
  website: string
}

export async function sendContactMessage(input: ContactInput): Promise<void> {
  await api.post('/api/public/contact', input)
}
