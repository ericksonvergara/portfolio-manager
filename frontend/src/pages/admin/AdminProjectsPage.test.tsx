import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import type { AdminProject } from '../../features/admin/projects/adminProjectsApi'
import { AdminProjectsPage } from './AdminProjectsPage'

/* Plain functions instead of vi.fn(), for the same reason as in LoginPage.test.tsx. */
const adminApi = vi.hoisted(() => ({
  answers: [] as Array<() => Promise<AdminProject[]>>,
}))

vi.mock('../../features/admin/projects/adminProjectsApi', () => ({
  listAdminProjects: () =>
    (adminApi.answers.shift() ?? (() => Promise.resolve([])))(),
}))

const PROJECT: AdminProject = {
  id: 7,
  slug: 'sistema-erp',
  name: 'Sistema ERP',
  shortDescription: 'Gestión de inventario.',
  description: 'Descripción completa.',
  coverImageUrl: null,
  projectUrl: null,
  repositoryUrl: null,
  videoUrl: null,
  status: 'IN_PROGRESS',
  developedOn: null,
  displayOrder: 2,
  featured: true,
  active: false,
  technologies: [
    { id: 1, name: 'Java' },
    { id: 2, name: 'React' },
  ],
}

function renderPage(state?: { created: string }) {
  render(
    <MemoryRouter initialEntries={[{ pathname: '/admin/proyectos', state }]}>
      <AdminProjectsPage />
    </MemoryRouter>,
  )
}

beforeEach(() => {
  adminApi.answers = []
})

describe('AdminProjectsPage', () => {
  it('lists every project with its status, publication and technologies', async () => {
    adminApi.answers.push(() => Promise.resolve([PROJECT]))
    renderPage()

    const row = (
      await screen.findByRole('rowheader', { name: /Sistema ERP/ })
    ).closest('tr')!
    expect(within(row).getByText('Destacado')).toBeInTheDocument()
    expect(within(row).getByText('En desarrollo')).toBeInTheDocument()
    expect(within(row).getByText('Inactivo')).toBeInTheDocument()
    expect(within(row).getByText('Java, React')).toBeInTheDocument()
    expect(
      screen.getByRole('link', { name: 'Registrar proyecto' }),
    ).toHaveAttribute('href', '/admin/proyectos/nuevo')
  })

  it('shows an empty state and the confirmation of a new project', async () => {
    renderPage({ created: 'Sistema ERP' })

    expect(
      await screen.findByText('Todavía no hay proyectos. Registra el primero.'),
    ).toBeInTheDocument()
    expect(screen.getByRole('status')).toHaveTextContent(
      'Proyecto «Sistema ERP» registrado.',
    )
  })

  it('retries after a network error', async () => {
    adminApi.answers.push(
      () => Promise.reject(new Error('network')),
      () => Promise.resolve([PROJECT]),
    )
    renderPage()

    await userEvent.click(
      await screen.findByRole('button', { name: 'Reintentar' }),
    )

    expect(
      await screen.findByRole('rowheader', { name: /Sistema ERP/ }),
    ).toBeInTheDocument()
  })
})
