import { isAxiosError } from 'axios'
import type { ProjectStatus } from '../../landing/landingApi'
import type { AdminProject, ProjectInput } from './adminProjectsApi'

/** Same rule as the backend: an absolute http(s) URL with a host (RN002, RN003). */
export function isWebUrl(value: string): boolean {
  try {
    const url = new URL(value)
    return (
      (url.protocol === 'http:' || url.protocol === 'https:') &&
      url.hostname !== ''
    )
  } catch {
    return false
  }
}

export interface ProjectFormValues {
  name: string
  shortDescription: string
  description: string
  projectUrl: string
  repositoryUrl: string
  videoUrl: string
  status: ProjectStatus
  developedOn: string
  displayOrder: string
  featured: boolean
  technologyIds: string[]
}

export const EMPTY_PROJECT: ProjectFormValues = {
  name: '',
  shortDescription: '',
  description: '',
  projectUrl: '',
  repositoryUrl: '',
  videoUrl: '',
  status: 'COMPLETED',
  developedOn: '',
  displayOrder: '0',
  featured: false,
  technologyIds: [],
}

export function toProjectInput(values: ProjectFormValues): ProjectInput {
  const optional = (value: string) => value.trim() || null
  return {
    name: values.name.trim(),
    shortDescription: values.shortDescription.trim(),
    description: values.description.trim(),
    projectUrl: optional(values.projectUrl),
    repositoryUrl: optional(values.repositoryUrl),
    videoUrl: optional(values.videoUrl),
    status: values.status,
    developedOn: optional(values.developedOn),
    displayOrder: Number(values.displayOrder),
    featured: values.featured,
    technologyIds: values.technologyIds.map(Number),
  }
}

/** The form rejects the submission with field errors; anything else is shown above the form. */
export class ProjectFormError extends Error {
  readonly field?: keyof ProjectFormValues

  constructor(message: string, field?: keyof ProjectFormValues) {
    super(message)
    this.field = field
  }
}

/** Turns a failed save into the message the form shows (409: the name is taken, RN001). */
export function toProjectFormError(error: unknown): ProjectFormError {
  if (isAxiosError(error) && error.response?.status === 409) {
    return new ProjectFormError('Ya existe un proyecto con ese nombre.', 'name')
  }
  if (isAxiosError(error) && error.response?.status === 400) {
    return new ProjectFormError('Revisa los datos ingresados.')
  }
  return new ProjectFormError(
    'No se pudo guardar el proyecto. Inténtalo de nuevo.',
  )
}

/** The form values of an existing project (HU016). */
export function fromAdminProject(project: AdminProject): ProjectFormValues {
  return {
    name: project.name,
    shortDescription: project.shortDescription,
    description: project.description,
    projectUrl: project.projectUrl ?? '',
    repositoryUrl: project.repositoryUrl ?? '',
    videoUrl: project.videoUrl ?? '',
    status: project.status,
    developedOn: project.developedOn ?? '',
    displayOrder: String(project.displayOrder),
    featured: project.featured,
    technologyIds: project.technologies.map((t) => String(t.id)),
  }
}
