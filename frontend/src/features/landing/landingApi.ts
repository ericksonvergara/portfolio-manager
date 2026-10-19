import { api } from '../../services/api'

export type SocialPlatform =
  'GITHUB' | 'LINKEDIN' | 'X' | 'INSTAGRAM' | 'YOUTUBE' | 'WEBSITE'

export type TechnologyCategory =
  'BACKEND' | 'FRONTEND' | 'DATABASE' | 'DEVOPS' | 'MOBILE' | 'TOOLS'

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
  name: string
  category: TechnologyCategory
}

export async function getProfile(): Promise<Profile> {
  const { data } = await api.get<Profile>('/api/public/profile')
  return data
}

export async function getTechnologies(): Promise<Technology[]> {
  const { data } = await api.get<Technology[]>('/api/public/technologies')
  return data
}
