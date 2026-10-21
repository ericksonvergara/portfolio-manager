import { describe, expect, it } from 'vitest'
import type { AdminProject } from './adminProjectsApi'
import {
  EMPTY_PROJECT,
  fromAdminProject,
  isWebUrl,
  toProjectInput,
} from './projectFormModel'

describe('isWebUrl', () => {
  it('accepts absolute http and https URLs only', () => {
    expect(isWebUrl('https://github.com/usuario/erp')).toBe(true)
    expect(isWebUrl('http://localhost:8081')).toBe(true)
    expect(isWebUrl('github.com/usuario/erp')).toBe(false)
    expect(isWebUrl('ftp://example.com')).toBe(false)
    expect(isWebUrl('javascript:alert(1)')).toBe(false)
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
