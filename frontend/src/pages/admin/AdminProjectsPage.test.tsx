import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import type { AdminProject } from '../../features/admin/projects/adminProjectsApi'
import { AdminProjectsPage } from './AdminProjectsPage'

/* Plain functions instead of vi.fn(), for the same reason as in LoginPage.test.tsx. */
const adminApi = vi.hoisted(() => ({
  answers: [] as Array<() => Promise<AdminProject[]>>,
  deleted: [] as number[],
  deleteAnswer: (): Promise<void> => Promise.resolve(),
}))

vi.mock('../../features/admin/projects/adminProjectsApi', () => ({
  listAdminProjects: () =>
    (adminApi.answers.shift() ?? (() => Promise.resolve([])))(),
  deleteProject: (id: number) => {
    adminApi.deleted.push(id)
    return adminApi.deleteAnswer()
  },
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

function renderPage(state?: { created?: string; updated?: string }) {
  render(
    <MemoryRouter initialEntries={[{ pathname: '/admin/proyectos', state }]}>
      <AdminProjectsPage />
    </MemoryRouter>,
  )
}

beforeEach(() => {
  adminApi.answers = []
  adminApi.deleted = []
  adminApi.deleteAnswer = () => Promise.resolve()
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
      within(row).getByRole('link', { name: 'Editar Sistema ERP' }),
    ).toHaveAttribute('href', '/admin/proyectos/7/editar')
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

  it('confirms an edited project', async () => {
    adminApi.answers.push(() => Promise.resolve([PROJECT]))
    renderPage({ updated: 'Sistema ERP' })

    expect(await screen.findByRole('status')).toHaveTextContent(
      'Proyecto «Sistema ERP» actualizado.',
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

  it('asks for confirmation and can be cancelled', async () => {
    adminApi.answers.push(() => Promise.resolve([PROJECT]))
    const user = userEvent.setup()
    renderPage()
    await user.click(
      await screen.findByRole('button', { name: 'Eliminar Sistema ERP' }),
    )

    const dialog = screen.getByRole('alertdialog', {
      name: 'Eliminar proyecto',
    })
    expect(dialog).toHaveTextContent('¿Eliminar «Sistema ERP»?')
    expect(
      within(dialog).getByRole('button', { name: 'Cancelar' }),
    ).toHaveFocus()

    await user.keyboard('{Escape}')
    expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument()
    expect(
      screen.getByRole('button', { name: 'Eliminar Sistema ERP' }),
    ).toHaveFocus()
    expect(adminApi.deleted).toHaveLength(0)
  })

  it('deletes the project after the confirmation', async () => {
    adminApi.answers.push(() => Promise.resolve([PROJECT]))
    const user = userEvent.setup()
    renderPage()
    await user.click(
      await screen.findByRole('button', { name: 'Eliminar Sistema ERP' }),
    )
    await user.click(
      within(screen.getByRole('alertdialog')).getByRole('button', {
        name: 'Eliminar',
      }),
    )

    expect(await screen.findByRole('status')).toHaveTextContent(
      'Proyecto «Sistema ERP» eliminado.',
    )
    expect(adminApi.deleted).toEqual([7])
    expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument()
    expect(
      screen.queryByRole('rowheader', { name: /Sistema ERP/ }),
    ).not.toBeInTheDocument()
    expect(
      screen.getByText('Todavía no hay proyectos. Registra el primero.'),
    ).toBeInTheDocument()
  })

  it('keeps the project and explains when the deletion fails', async () => {
    adminApi.answers.push(() => Promise.resolve([PROJECT]))
    adminApi.deleteAnswer = () => Promise.reject(new Error('network'))
    const user = userEvent.setup()
    renderPage()
    await user.click(
      await screen.findByRole('button', { name: 'Eliminar Sistema ERP' }),
    )
    await user.click(
      within(screen.getByRole('alertdialog')).getByRole('button', {
        name: 'Eliminar',
      }),
    )

    expect(
      await within(screen.getByRole('alertdialog')).findByRole('alert'),
    ).toHaveTextContent('No se pudo eliminar el proyecto. Inténtalo de nuevo.')
    expect(
      screen.getByRole('rowheader', { name: /Sistema ERP/ }),
    ).toBeInTheDocument()
  })
})
