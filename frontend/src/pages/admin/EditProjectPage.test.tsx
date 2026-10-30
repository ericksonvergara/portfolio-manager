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

function httpError(status: number, message?: string) {
  return new AxiosError(
    'Request failed',
    'ERR_BAD_REQUEST',
    undefined,
    undefined,
    {
      status,
      data: { message },
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

  it('removes the technologies that are unchecked (HU022)', async () => {
    api.project = () =>
      Promise.resolve({
        ...PROJECT,
        technologies: [
          { id: 1, name: 'Java' },
          { id: 2, name: 'React' },
        ],
      })
    api.respond = () => Promise.resolve(PROJECT)
    const user = userEvent.setup()
    renderPage()
    await user.click(await screen.findByLabelText('React'))
    await user.click(screen.getByRole('button', { name: 'Guardar cambios' }))

    expect(await screen.findByText(/Listado:/)).toBeInTheDocument()
    expect(api.updates[0].input.technologyIds).toEqual([1])
  })

  it('asks to keep at least one technology', async () => {
    const user = userEvent.setup()
    renderPage()
    await user.click(await screen.findByLabelText('Java'))
    await user.click(screen.getByRole('button', { name: 'Guardar cambios' }))

    expect(
      await screen.findByText('Selecciona al menos una tecnología.'),
    ).toBeInTheDocument()
    expect(api.updates).toEqual([])
  })

  it('explains that a technology left the catalog while editing', async () => {
    api.respond = () =>
      Promise.reject(httpError(400, 'Unknown technologies: [2]'))
    const user = userEvent.setup()
    renderPage()
    await user.click(
      await screen.findByRole('button', { name: 'Guardar cambios' }),
    )

    expect(
      await screen.findByText(
        /Alguna tecnología seleccionada ya no existe en el catálogo/,
      ),
    ).toBeInTheDocument()
  })

  it('asks to reload when the data changed while saving', async () => {
    api.respond = () =>
      Promise.reject(
        httpError(
          409,
          'The data changed while saving. Reload it and try again',
        ),
      )
    const user = userEvent.setup()
    renderPage()
    await user.click(
      await screen.findByRole('button', { name: 'Guardar cambios' }),
    )

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Los datos cambiaron mientras guardabas.',
    )
    expect(screen.getByLabelText('Nombre')).toHaveAttribute(
      'aria-invalid',
      'false',
    )
  })

  it('marks the name when another project already has it', async () => {
    api.respond = () =>
      Promise.reject(
        httpError(
          409,
          "A project named 'Sistema ERP', or with an equivalent name, already exists",
        ),
      )
    const user = userEvent.setup()
    renderPage()
    await user.click(
      await screen.findByRole('button', { name: 'Guardar cambios' }),
    )

    expect(
      await screen.findByText(
        /Ya existe un proyecto con ese nombre o con uno equivalente/,
      ),
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
