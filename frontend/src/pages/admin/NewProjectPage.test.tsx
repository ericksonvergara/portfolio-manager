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
import { NewProjectPage } from './NewProjectPage'

/* Plain functions instead of vi.fn(), for the same reason as in LoginPage.test.tsx. */
const api = vi.hoisted(() => ({
  created: [] as ProjectInput[],
  respond: (): Promise<AdminProject> => Promise.reject(new Error('not set')),
}))

const TECHNOLOGIES: Technology[] = [
  { id: 1, name: 'Java', category: 'BACKEND' },
  { id: 2, name: 'React', category: 'FRONTEND' },
]

vi.mock('../../features/admin/projects/adminProjectsApi', () => ({
  createProject: (input: ProjectInput) => {
    api.created.push(input)
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

function EditPage() {
  const state = useLocation().state as { created?: string } | null
  return <p>Edición: {state?.created}</p>
}

function renderPage() {
  render(
    <MemoryRouter initialEntries={['/admin/proyectos/nuevo']}>
      <Routes>
        <Route path="/admin/proyectos/nuevo" element={<NewProjectPage />} />
        <Route path="/admin/proyectos/:id/editar" element={<EditPage />} />
      </Routes>
    </MemoryRouter>,
  )
}

async function fillRequiredFields() {
  const user = userEvent.setup()
  await user.type(await screen.findByLabelText('Nombre'), 'Sistema ERP')
  await user.type(
    screen.getByLabelText('Descripción corta'),
    'Gestión de inventario.',
  )
  await user.type(
    screen.getByLabelText('Descripción completa'),
    'Descripción completa.',
  )
  await user.click(screen.getByLabelText('Java'))
  return user
}

beforeEach(() => {
  api.created = []
  api.respond = () => Promise.reject(new Error('not set'))
})

describe('NewProjectPage', () => {
  it('shows the validation messages and sends nothing', async () => {
    const user = userEvent.setup()
    renderPage()
    await user.type(
      await screen.findByLabelText(/URL del proyecto/),
      'erp.example.com',
    )
    await user.click(screen.getByRole('button', { name: 'Registrar proyecto' }))

    expect(
      await screen.findByText('El nombre es obligatorio.'),
    ).toBeInTheDocument()
    expect(
      screen.getByText('La descripción corta es obligatoria.'),
    ).toBeInTheDocument()
    expect(
      screen.getByText('La descripción completa es obligatoria.'),
    ).toBeInTheDocument()
    expect(
      screen.getByText(
        'Ingresa una URL completa y válida, por ejemplo https://ejemplo.com.',
      ),
    ).toBeInTheDocument()
    expect(
      screen.getByText('Selecciona al menos una tecnología.'),
    ).toBeInTheDocument()
    expect(screen.getByLabelText('Nombre')).toHaveAttribute(
      'aria-invalid',
      'true',
    )
    expect(api.created).toHaveLength(0)
  })

  it('registers the project and opens it to add its images', async () => {
    api.respond = () =>
      Promise.resolve({ id: 7, name: 'Sistema ERP' } as AdminProject)
    renderPage()
    const user = await fillRequiredFields()
    await user.type(
      screen.getByLabelText(/Repositorio/),
      'https://github.com/usuario/erp',
    )
    await user.selectOptions(screen.getByLabelText('Estado'), 'IN_PROGRESS')
    await user.click(screen.getByLabelText(/Destacado/))
    await user.click(screen.getByRole('button', { name: 'Registrar proyecto' }))

    expect(await screen.findByText('Edición: Sistema ERP')).toBeInTheDocument()
    expect(api.created).toEqual([
      {
        name: 'Sistema ERP',
        shortDescription: 'Gestión de inventario.',
        description: 'Descripción completa.',
        projectUrl: null,
        repositoryUrl: 'https://github.com/usuario/erp',
        videoUrl: null,
        status: 'IN_PROGRESS',
        developedOn: null,
        displayOrder: 0,
        featured: true,
        technologyIds: [1],
      },
    ])
  })

  it('marks the name when it is already taken', async () => {
    api.respond = () =>
      Promise.reject(
        httpError(
          409,
          "A project named 'Sistema ERP', or with an equivalent name, already exists",
        ),
      )
    renderPage()
    const user = await fillRequiredFields()
    await user.click(screen.getByRole('button', { name: 'Registrar proyecto' }))

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

  it('shows a general message when the server cannot be reached', async () => {
    api.respond = () => Promise.reject(new Error('network'))
    renderPage()
    const user = await fillRequiredFields()
    await user.click(screen.getByRole('button', { name: 'Registrar proyecto' }))

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'No se pudo guardar el proyecto. Inténtalo de nuevo.',
    )
  })
})
