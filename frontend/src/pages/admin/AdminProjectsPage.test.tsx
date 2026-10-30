import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { AxiosError, type AxiosResponse } from 'axios'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import type { AdminProject } from '../../features/admin/projects/adminProjectsApi'
import { AdminProjectsPage } from './AdminProjectsPage'

/* Plain functions instead of vi.fn(), for the same reason as in LoginPage.test.tsx. */
const adminApi = vi.hoisted(() => ({
  answers: [] as Array<() => Promise<AdminProject[]>>,
  deleted: [] as number[],
  deleteAnswer: (): Promise<void> => Promise.resolve(),
  published: [] as Array<{ id: number; active: boolean }>,
  publishAnswer: (): Promise<AdminProject> =>
    Promise.reject(new Error('not set')),
}))

vi.mock('../../features/admin/projects/adminProjectsApi', () => ({
  listAdminProjects: () =>
    (adminApi.answers.shift() ?? (() => Promise.resolve([])))(),
  deleteProject: (id: number) => {
    adminApi.deleted.push(id)
    return adminApi.deleteAnswer()
  },
  setProjectPublication: (id: number, active: boolean) => {
    adminApi.published.push({ id, active })
    return adminApi.publishAnswer()
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
  gallery: [],
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
  adminApi.published = []
  adminApi.publishAnswer = () => Promise.reject(new Error('not set'))
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

  it('publishes a project with a switch', async () => {
    adminApi.answers.push(() =>
      Promise.resolve([{ ...PROJECT, coverImageUrl: '/uploads/erp.png' }]),
    )
    adminApi.publishAnswer = () =>
      Promise.resolve({
        ...PROJECT,
        coverImageUrl: '/uploads/erp.png',
        active: true,
      })
    const user = userEvent.setup()
    renderPage()
    const toggle = await screen.findByRole('switch', {
      name: 'Publicar Sistema ERP',
    })
    expect(toggle).not.toBeChecked()

    await user.click(toggle)

    expect(await screen.findByRole('status')).toHaveTextContent(
      '«Sistema ERP» está publicado en el portafolio.',
    )
    expect(toggle).toBeChecked()
    expect(adminApi.published).toEqual([{ id: 7, active: true }])
  })

  it('explains that a project needs a cover image to be published', async () => {
    adminApi.answers.push(() => Promise.resolve([PROJECT]))
    adminApi.publishAnswer = () =>
      Promise.reject(
        new AxiosError('Conflict', 'ERR_BAD_REQUEST', undefined, undefined, {
          status: 409,
        } as AxiosResponse),
      )
    const user = userEvent.setup()
    renderPage()
    const toggle = await screen.findByRole('switch', {
      name: 'Publicar Sistema ERP',
    })

    await user.click(toggle)

    expect(await screen.findByRole('alert')).toHaveTextContent(
      '«Sistema ERP» necesita una imagen principal para publicarse.',
    )
    expect(toggle).not.toBeChecked()
  })

  it('tells in advance what an inactive project lacks to be published (HU030)', async () => {
    adminApi.answers.push(() =>
      Promise.resolve([
        PROJECT,
        {
          ...PROJECT,
          id: 8,
          name: 'Tienda',
          coverImageUrl: null,
          technologies: [],
        },
        { ...PROJECT, id: 9, name: 'Blog', coverImageUrl: '/uploads/blog.png' },
      ]),
    )
    renderPage()

    expect(
      await screen.findByRole('switch', { name: 'Publicar Sistema ERP' }),
    ).toHaveAccessibleDescription('Para publicarlo falta una imagen principal.')
    expect(
      screen.getByRole('switch', { name: 'Publicar Tienda' }),
    ).toHaveAccessibleDescription(
      'Para publicarlo falta una imagen principal y al menos una tecnología.',
    )
    expect(
      screen.getByRole('switch', { name: 'Publicar Blog' }),
    ).not.toHaveAccessibleDescription()
  })

  it('names everything a rejected project lacks', async () => {
    adminApi.answers.push(() =>
      Promise.resolve([{ ...PROJECT, technologies: [] }]),
    )
    adminApi.publishAnswer = () =>
      Promise.reject(
        new AxiosError('Conflict', 'ERR_BAD_REQUEST', undefined, undefined, {
          status: 409,
        } as AxiosResponse),
      )
    const user = userEvent.setup()
    renderPage()
    await user.click(
      await screen.findByRole('switch', { name: 'Publicar Sistema ERP' }),
    )

    expect(await screen.findByRole('alert')).toHaveTextContent(
      '«Sistema ERP» necesita una imagen principal y al menos una tecnología para publicarse.',
    )
  })
})
