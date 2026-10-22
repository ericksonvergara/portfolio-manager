import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { AxiosError, type AxiosResponse } from 'axios'
import { MemoryRouter, Route, Routes, useLocation } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import type {
  AdminProject,
  ProjectInput,
} from '../../features/admin/projects/adminProjectsApi'
import type { Technology } from '../../features/landing/landingApi'
import { EditProjectPage } from './EditProjectPage'

/* Plain functions instead of vi.fn(), for the same reason as in LoginPage.test.tsx. */
const api = vi.hoisted(() => ({
  requested: [] as number[],
  project: (): Promise<AdminProject> => Promise.reject(new Error('not set')),
  updates: [] as Array<{ id: number; input: ProjectInput }>,
  respond: (): Promise<AdminProject> => Promise.reject(new Error('not set')),
}))

const TECHNOLOGIES: Technology[] = [
  { id: 1, name: 'Java', category: 'BACKEND' },
  { id: 2, name: 'React', category: 'FRONTEND' },
]

const PROJECT: AdminProject = {
  id: 7,
  slug: 'sistema-erp',
  name: 'Sistema ERP',
  shortDescription: 'Gestión de inventario.',
  description: 'Descripción completa.',
  coverImageUrl: '/uploads/erp.png',
  projectUrl: 'https://erp.example.com',
  repositoryUrl: null,
  videoUrl: null,
  status: 'COMPLETED',
  developedOn: '2026-10-01',
  displayOrder: 2,
  featured: false,
  active: true,
  technologies: [{ id: 1, name: 'Java' }],
  gallery: [],
}

vi.mock('../../features/admin/projects/adminProjectsApi', () => ({
  getAdminProject: (id: number) => {
    api.requested.push(id)
    return api.project()
  },
  updateProject: (id: number, input: ProjectInput) => {
    api.updates.push({ id, input })
    return api.respond()
  },
}))

vi.mock('../../features/landing/landingApi', () => ({
  getTechnologies: () => Promise.resolve(TECHNOLOGIES),
}))

function httpError(status: number) {
  return new AxiosError(
    'Request failed',
    'ERR_BAD_REQUEST',
    undefined,
    undefined,
    {
      status,
    } as AxiosResponse,
  )
}

function ListPage() {
  const state = useLocation().state as { updated?: string } | null
  return <p>Listado: {state?.updated}</p>
}

function renderPage(id = '7') {
  render(
    <MemoryRouter initialEntries={[`/admin/proyectos/${id}/editar`]}>
      <Routes>
        <Route
          path="/admin/proyectos/:id/editar"
          element={<EditProjectPage />}
        />
        <Route path="/admin/proyectos" element={<ListPage />} />
      </Routes>
    </MemoryRouter>,
  )
}

beforeEach(() => {
  api.requested = []
  api.updates = []
  api.project = () => Promise.resolve(PROJECT)
  api.respond = () => Promise.reject(new Error('not set'))
})

describe('EditProjectPage', () => {
  it('loads the project into the form', async () => {
    renderPage()

    expect(await screen.findByLabelText('Nombre')).toHaveValue('Sistema ERP')
    expect(api.requested).toEqual([7])
    expect(screen.getByLabelText(/URL del proyecto/)).toHaveValue(
      'https://erp.example.com',
    )
    expect(screen.getByLabelText(/Fecha/)).toHaveValue('2026-10-01')
    expect(screen.getByLabelText('Orden de aparición')).toHaveValue(2)
    expect(screen.getByLabelText('Java')).toBeChecked()
    expect(screen.getByLabelText('React')).not.toBeChecked()
    expect(screen.getByText('/proyectos/sistema-erp')).toBeInTheDocument()
  })

  it('saves the changes and returns to the list', async () => {
    api.respond = () =>
      Promise.resolve({ ...PROJECT, name: 'Sistema ERP Cloud' })
    const user = userEvent.setup()
    renderPage()
    const name = await screen.findByLabelText('Nombre')
    await user.clear(name)
    await user.type(name, 'Sistema ERP Cloud')
    await user.click(screen.getByLabelText('React'))
    await user.click(screen.getByRole('button', { name: 'Guardar cambios' }))

    expect(
      await screen.findByText('Listado: Sistema ERP Cloud'),
    ).toBeInTheDocument()
    expect(api.updates).toEqual([
      {
        id: 7,
        input: {
          name: 'Sistema ERP Cloud',
          shortDescription: 'Gestión de inventario.',
          description: 'Descripción completa.',
          projectUrl: 'https://erp.example.com',
          repositoryUrl: null,
          videoUrl: null,
          status: 'COMPLETED',
          developedOn: '2026-10-01',
          displayOrder: 2,
          featured: false,
          technologyIds: [1, 2],
        },
      },
    ])
  })

  it('marks the name when another project already has it', async () => {
    api.respond = () => Promise.reject(httpError(409))
    const user = userEvent.setup()
    renderPage()
    await user.click(
      await screen.findByRole('button', { name: 'Guardar cambios' }),
    )

    expect(
      await screen.findByText('Ya existe un proyecto con ese nombre.'),
    ).toBeInTheDocument()
    expect(screen.getByLabelText('Nombre')).toHaveAttribute(
      'aria-invalid',
      'true',
    )
  })

  it('shows a not found message for an unknown or invalid project', async () => {
    api.project = () => Promise.reject(httpError(404))
    renderPage()
    expect(
      await screen.findByText(/Proyecto no encontrado/),
    ).toBeInTheDocument()
    expect(
      screen.getByRole('link', { name: '← Volver a proyectos' }),
    ).toHaveAttribute('href', '/admin/proyectos')
  })
})
