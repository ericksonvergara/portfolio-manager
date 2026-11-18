import { render, screen, within } from '@testing-library/react'
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
  latestPublishedProject: {
    name: 'Tienda en línea',
    publishedAt: '2026-11-03T10:00:00Z',
  },
  lastAccessAt: '2026-11-04T21:15:00Z',
  visits: {
    total: 12583,
    today: 54,
    thisMonth: 812,
    mostVisitedProject: { name: 'Agenda de citas', visits: 320 },
  },
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

  it('shows the visit statistics (HU013)', async () => {
    render(<DashboardPage />)
    const visits = await screen.findByRole('region', { name: 'Visitas' })

    expect(within(visits).getByText('Visitas totales')).toBeInTheDocument()
    expect(
      within(visits).getByText((12583).toLocaleString('es')),
    ).toBeInTheDocument()
    expect(within(visits).getByText('54')).toBeInTheDocument()
    expect(within(visits).getByText('812')).toBeInTheDocument()
    expect(within(visits).getByText('Agenda de citas')).toBeInTheDocument()
    expect(within(visits).getByText('320 visitas')).toBeInTheDocument()
  })

  it('shows the latest published project next to the portfolio data', async () => {
    render(<DashboardPage />)
    const portfolio = await screen.findByRole('region', { name: 'Portafolio' })

    expect(within(portfolio).getByText('Tienda en línea')).toBeInTheDocument()
    expect(
      within(portfolio).getByText(dateTime('2026-11-03T10:00:00Z')),
    ).toBeInTheDocument()
  })

  it('explains an empty portfolio and a first session', async () => {
    api.answer = () =>
      Promise.resolve({
        totalProjects: 0,
        totalTechnologies: 0,
        latestProject: null,
        latestPublishedProject: null,
        lastAccessAt: null,
        visits: { total: 0, today: 0, thisMonth: 0, mostVisitedProject: null },
      })
    render(<DashboardPage />)

    expect(await screen.findAllByText('Ninguno todavía')).toHaveLength(3)
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
