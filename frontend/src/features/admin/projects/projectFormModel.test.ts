import { AxiosError, type AxiosResponse } from 'axios'
import { describe, expect, it } from 'vitest'
import type { AdminProject } from './adminProjectsApi'
import {
  EMPTY_PROJECT,
  fromAdminProject,
  toProjectFormError,
  toProjectInput,
} from './projectFormModel'

function badRequest(message: string) {
  return new AxiosError(
    'Request failed',
    'ERR_BAD_REQUEST',
    undefined,
    undefined,
    {
      status: 400,
      data: { message },
    } as AxiosResponse,
  )
}

describe('toProjectFormError (HU029)', () => {
  it('shows an invalid link rejected by the server next to its field', () => {
    for (const field of ['projectUrl', 'repositoryUrl', 'videoUrl'] as const) {
      const error = toProjectFormError(
        badRequest(`${field} must be a valid http or https URL`),
      )
      expect(error.field).toBe(field)
      expect(error.message).toMatch(/Ingresa una URL completa y válida/)
    }
  })

  it('keeps other rejections above the form', () => {
    expect(
      toProjectFormError(badRequest('name must not be blank')).field,
    ).toBeUndefined()
  })
})

describe('toProjectInput', () => {
  it('trims the text, turns blank optional fields into null and parses numbers', () => {
    expect(
      toProjectInput({
        ...EMPTY_PROJECT,
        name: '  Sistema ERP ',
        shortDescription: ' Corta ',
        description: 'Completa',
        projectUrl: '  ',
        repositoryUrl: ' https://github.com/usuario/erp ',
        developedOn: '',
        displayOrder: '3',
        featured: true,
        technologyIds: ['2', '5'],
      }),
    ).toEqual({
      name: 'Sistema ERP',
      shortDescription: 'Corta',
      description: 'Completa',
      projectUrl: null,
      repositoryUrl: 'https://github.com/usuario/erp',
      videoUrl: null,
      status: 'COMPLETED',
      developedOn: null,
      displayOrder: 3,
      featured: true,
      technologyIds: [2, 5],
    })
  })
})

describe('fromAdminProject', () => {
  it('turns an existing project into form values that round-trip', () => {
    const project: AdminProject = {
      id: 7,
      slug: 'sistema-erp',
      name: 'Sistema ERP',
      shortDescription: 'Corta',
      description: 'Completa',
      coverImageUrl: null,
      projectUrl: null,
      repositoryUrl: 'https://github.com/usuario/erp',
      videoUrl: null,
      status: 'IN_PROGRESS',
      developedOn: null,
      displayOrder: 4,
      featured: true,
      active: false,
      technologies: [{ id: 3, name: 'Java' }],
      gallery: [],
    }

    const values = fromAdminProject(project)

    expect(values.projectUrl).toBe('')
    expect(values.displayOrder).toBe('4')
    expect(values.technologyIds).toEqual(['3'])
    expect(toProjectInput(values)).toMatchObject({
      repositoryUrl: 'https://github.com/usuario/erp',
      projectUrl: null,
      developedOn: null,
      displayOrder: 4,
      technologyIds: [3],
    })
  })
})
