import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { AxiosError, type AxiosResponse } from 'axios'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import type {
  AdminTechnology,
  TechnologyInput,
} from '../../features/admin/technologies/adminTechnologiesApi'
import { TechnologiesPage } from './TechnologiesPage'

/* Plain functions instead of vi.fn(), for the same reason as in LoginPage.test.tsx. */
const api = vi.hoisted(() => ({
  calls: [] as string[],
  catalog: [] as AdminTechnology[],
  createAnswer: (input: TechnologyInput): Promise<AdminTechnology> =>
    Promise.resolve({ id: 99, projectCount: 0, ...input }),
  deleteAnswer: (): Promise<void> => Promise.resolve(),
}))

vi.mock('../../features/admin/technologies/adminTechnologiesApi', () => ({
  listAdminTechnologies: () => Promise.resolve(api.catalog),
  createTechnology: (input: TechnologyInput) => {
    api.calls.push(
      `create ${input.name} ${input.category} ${input.displayOrder}`,
    )
    return api.createAnswer(input)
  },
  updateTechnology: (id: number, input: TechnologyInput) => {
    api.calls.push(`update ${id} ${input.name}`)
    return Promise.resolve({ id, projectCount: 2, ...input })
  },
  deleteTechnology: (id: number) => {
    api.calls.push(`delete ${id}`)
    return api.deleteAnswer()
  },
}))

function conflict() {
  return new AxiosError('Conflict', 'ERR_BAD_REQUEST', undefined, undefined, {
    status: 409,
  } as AxiosResponse)
}

beforeEach(() => {
  api.calls = []
  api.catalog = [
    {
      id: 1,
      name: 'Java',
      category: 'BACKEND',
      displayOrder: 1,
      projectCount: 2,
    },
    {
      id: 2,
      name: 'Vue',
      category: 'FRONTEND',
      displayOrder: 2,
      projectCount: 0,
    },
  ]
  api.createAnswer = (input) =>
    Promise.resolve({ id: 99, projectCount: 0, ...input })
  api.deleteAnswer = () => Promise.resolve()
})

describe('TechnologiesPage', () => {
  it('lists the catalog with its category and usage', async () => {
    render(<TechnologiesPage />)

    const java = (
      await screen.findByRole('rowheader', { name: 'Java' })
    ).closest('tr')!
    expect(within(java).getByText('Backend')).toBeInTheDocument()
    expect(within(java).getByText('2')).toBeInTheDocument()
    // RN005: a technology in use cannot be deleted
    expect(
      within(java).getByRole('button', {
        name: 'No se puede eliminar Java: la usan 2 proyecto(s)',
      }),
    ).toBeDisabled()
  })

  it('adds a technology', async () => {
    const user = userEvent.setup()
    render(<TechnologiesPage />)
    await user.type(await screen.findByLabelText('Nombre'), '  Kotlin ')
    await user.clear(screen.getByLabelText('Orden'))
    await user.type(screen.getByLabelText('Orden'), '3')
    await user.click(screen.getByRole('button', { name: 'Agregar tecnología' }))

    expect(
      await screen.findByRole('rowheader', { name: 'Kotlin' }),
    ).toBeInTheDocument()
    expect(screen.getByRole('status')).toHaveTextContent(
      'Tecnología «Kotlin» agregada.',
    )
    expect(api.calls).toEqual(['create Kotlin BACKEND 3'])
    expect(screen.getByLabelText('Nombre')).toHaveValue('')
  })

  it('marks a duplicated name', async () => {
    api.createAnswer = () => Promise.reject(conflict())
    const user = userEvent.setup()
    render(<TechnologiesPage />)
    await user.type(await screen.findByLabelText('Nombre'), 'java')
    await user.click(screen.getByRole('button', { name: 'Agregar tecnología' }))

    expect(
      await screen.findByText('Ya existe una tecnología con ese nombre.'),
    ).toBeInTheDocument()
    expect(screen.getByLabelText('Nombre')).toHaveAttribute(
      'aria-invalid',
      'true',
    )
  })

  it('edits a technology in the same form', async () => {
    const user = userEvent.setup()
    render(<TechnologiesPage />)
    await user.click(await screen.findByRole('button', { name: 'Editar Java' }))

    expect(
      screen.getByRole('heading', { name: 'Editar «Java»' }),
    ).toBeInTheDocument()
    expect(screen.getByLabelText('Nombre')).toHaveValue('Java')
    await user.clear(screen.getByLabelText('Nombre'))
    await user.type(screen.getByLabelText('Nombre'), 'Java 21')
    await user.click(screen.getByRole('button', { name: 'Guardar cambios' }))

    expect(
      await screen.findByRole('rowheader', { name: 'Java 21' }),
    ).toBeInTheDocument()
    expect(api.calls).toEqual(['update 1 Java 21'])
    expect(
      screen.getByRole('heading', { name: 'Nueva tecnología' }),
    ).toBeInTheDocument()
  })

  it('deletes an unused technology after a confirmation', async () => {
    const user = userEvent.setup()
    render(<TechnologiesPage />)
    await user.click(
      await screen.findByRole('button', { name: 'Eliminar Vue' }),
    )
    await user.click(
      within(screen.getByRole('alertdialog')).getByRole('button', {
        name: 'Eliminar',
      }),
    )

    expect(await screen.findByRole('status')).toHaveTextContent(
      'Tecnología «Vue» eliminada.',
    )
    expect(
      screen.queryByRole('rowheader', { name: 'Vue' }),
    ).not.toBeInTheDocument()
    expect(api.calls).toEqual(['delete 2'])
  })

  it('explains when a project started using it meanwhile', async () => {
    api.deleteAnswer = () => Promise.reject(conflict())
    const user = userEvent.setup()
    render(<TechnologiesPage />)
    await user.click(
      await screen.findByRole('button', { name: 'Eliminar Vue' }),
    )
    await user.click(
      within(screen.getByRole('alertdialog')).getByRole('button', {
        name: 'Eliminar',
      }),
    )

    expect(
      await within(screen.getByRole('alertdialog')).findByRole('alert'),
    ).toHaveTextContent('La usa al menos un proyecto')
    expect(screen.getByRole('rowheader', { name: 'Vue' })).toBeInTheDocument()
  })
})
