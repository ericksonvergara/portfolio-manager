import { api } from '../../../services/api'
import type { TechnologyCategory } from '../../landing/landingApi'

/** A technology of the catalog, with the number of projects that use it (HU021). */
export interface AdminTechnology {
  id: number
  name: string
  category: TechnologyCategory
  displayOrder: number
  projectCount: number
}

export interface TechnologyInput {
  name: string
  category: TechnologyCategory
  displayOrder: number
}

export async function listAdminTechnologies(): Promise<AdminTechnology[]> {
  const { data } = await api.get<AdminTechnology[]>('/api/admin/technologies')
  return data
}

export async function createTechnology(
  input: TechnologyInput,
): Promise<AdminTechnology> {
  const { data } = await api.post<AdminTechnology>(
    '/api/admin/technologies',
    input,
  )
  return data
}

export async function updateTechnology(
  id: number,
  input: TechnologyInput,
): Promise<AdminTechnology> {
  const { data } = await api.put<AdminTechnology>(
    `/api/admin/technologies/${id}`,
    input,
  )
  return data
}

export async function deleteTechnology(id: number): Promise<void> {
  await api.delete(`/api/admin/technologies/${id}`)
}
