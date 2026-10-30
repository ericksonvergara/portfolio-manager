import { isAxiosError } from 'axios'
import { WEB_URL_MESSAGE } from '../../../services/validation'
import type { ProjectStatus } from '../../landing/landingApi'
import type { AdminProject, ProjectInput } from './adminProjectsApi'

export { isWebUrl } from '../../../services/validation'

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

/** The backend messages that point to one field of the form (see GlobalExceptionHandler). */
const DUPLICATED_NAME = /^A project named /
const UNKNOWN_TECHNOLOGIES = /^Unknown technologies/
const INVALID_URL = /^(projectUrl|repositoryUrl|videoUrl) must be a valid/

/**
 * Turns a failed save into the message the form shows: the name is taken (409, RN001), a technology was
 * deleted from the catalog while the form was open (400, HU022) or the data changed meanwhile (other 409).
 */
export function toProjectFormError(error: unknown): ProjectFormError {
  const status = isAxiosError(error) ? error.response?.status : undefined
  const message = isAxiosError<{ message?: string }>(error)
    ? (error.response?.data?.message ?? '')
    : ''
  if (status === 409 && DUPLICATED_NAME.test(message)) {
    return new ProjectFormError(
      'Ya existe un proyecto con ese nombre o con uno equivalente (solo cambian mayúsculas, tildes o signos).',
      'name',
    )
  }
  if (status === 409) {
    return new ProjectFormError(
      'Los datos cambiaron mientras guardabas. Recarga la página e inténtalo de nuevo.',
    )
  }
  const invalidUrl = status === 400 ? INVALID_URL.exec(message) : null
  if (invalidUrl) {
    return new ProjectFormError(
      WEB_URL_MESSAGE,
      invalidUrl[1] as keyof ProjectFormValues,
    )
  }
  if (status === 400 && UNKNOWN_TECHNOLOGIES.test(message)) {
    return new ProjectFormError(
      'Alguna tecnología seleccionada ya no existe en el catálogo. Recarga la página para ver el catálogo actual.',
      'technologyIds',
    )
  }
  if (status === 400) {
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

/**
 * What a project still lacks to be published (HU030): a cover image (RN004) and at least one
 * technology (RN005). The backend checks the same.
 */
export function publicationRequirements(project: AdminProject): string[] {
  return [
    ...(project.coverImageUrl === null ? ['una imagen principal'] : []),
    ...(project.technologies.length === 0 ? ['al menos una tecnología'] : []),
  ]
}
