import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import type { DashboardSummary } from '../../features/admin/dashboard/adminDashboardApi'
import { DashboardPage } from './DashboardPage'

/* Plain functions instead of vi.fn(), for the same reason as in LoginPage.test.tsx. */
const api = vi.hoisted(() => ({
  answer: (): Promise<DashboardSummary> => Promise.reject(new Error('not set')),
}))

vi.mock('../../features/admin/dashboard/adminDashboardApi', () => ({
  getDashboard: () => api.answer(),
}))

const SUMMARY: DashboardSummary = {
  totalProjects: 1200,
  totalTechnologies: 24,
  latestProject: {
    name: 'Sistema ERP',
    registeredAt: '2026-11-02T15:30:00Z',
  },
  lastAccessAt: '2026-11-04T21:15:00Z',
}

const dateTime = (value: string) =>
  new Intl.DateTimeFormat('es', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))

beforeEach(() => {
  api.answer = () => Promise.resolve(SUMMARY)
})

describe('DashboardPage (HU012)', () => {
  it('shows the general indicators', async () => {
    render(<DashboardPage />)

    expect(
      await screen.findByText((1200).toLocaleString('es')),
    ).toBeInTheDocument()
    expect(screen.getByText('24')).toBeInTheDocument()
    expect(screen.getByText('Sistema ERP')).toBeInTheDocument()
    expect(
      screen.getByText(dateTime('2026-11-02T15:30:00Z')),
    ).toBeInTheDocument()
    expect(
      screen.getByText(dateTime('2026-11-04T21:15:00Z')),
    ).toBeInTheDocument()
  })

  it('explains an empty portfolio and a first session', async () => {
    api.answer = () =>
      Promise.resolve({
        totalProjects: 0,
        totalTechnologies: 0,
        latestProject: null,
        lastAccessAt: null,
      })
    render(<DashboardPage />)

    expect(await screen.findByText('Ninguno todavía')).toBeInTheDocument()
    expect(screen.getByText('Este es tu primer acceso')).toBeInTheDocument()
  })

  it('offers to retry when the indicators cannot be loaded', async () => {
    api.answer = () => Promise.reject(new Error('network'))
    const user = userEvent.setup()
    render(<DashboardPage />)

    expect(
      await screen.findByText('No se pudieron cargar los indicadores.'),
    ).toBeInTheDocument()

    api.answer = () => Promise.resolve(SUMMARY)
    await user.click(screen.getByRole('button', { name: 'Reintentar' }))
    expect(await screen.findByText('Sistema ERP')).toBeInTheDocument()
  })
})
