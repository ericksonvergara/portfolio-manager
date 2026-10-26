import { isAxiosError } from 'axios'
import { api } from '../../../services/api'
import type { Profile, SocialLink } from '../../landing/landingApi'

/** The data of the profile form; the photo is uploaded apart (HU023). */
export interface ProfileInput {
  fullName: string
  headline: string
  about: string
  email: string
  location: string | null
  socialLinks: SocialLink[]
}

const MULTIPART = { headers: { 'Content-Type': 'multipart/form-data' } }

/** The public profile, or null while it has not been created (production has no demo seed). */
export async function getAdminProfile(): Promise<Profile | null> {
  try {
    const { data } = await api.get<Profile>('/api/admin/profile')
    return data
  } catch (error) {
    if (isAxiosError(error) && error.response?.status === 404) return null
    throw error
  }
}

/** Saves the profile, creating it the first time; the photo is kept. */
export async function saveProfile(input: ProfileInput): Promise<Profile> {
  const { data } = await api.put<Profile>('/api/admin/profile', input)
  return data
}

export async function uploadProfilePhoto(file: File): Promise<Profile> {
  const form = new FormData()
  form.append('file', file)
  const { data } = await api.put<Profile>(
    '/api/admin/profile/photo',
    form,
    MULTIPART,
  )
  return data
}

export async function removeProfilePhoto(): Promise<Profile> {
  const { data } = await api.delete<Profile>('/api/admin/profile/photo')
  return data
}
