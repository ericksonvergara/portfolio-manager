import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { act } from 'react'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
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
    ranking: [
      { id: 3, name: 'Agenda de citas', visits: 320, published: true },
      { id: 7, name: 'Sistema ERP', visits: 210, published: false },
    ],
  },
}

const dateTime = (value: string) =>
  new Intl.DateTimeFormat('es', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))

function renderPage() {
  render(
    <MemoryRouter>
      <DashboardPage />
    </MemoryRouter>,
  )
}

beforeEach(() => {
  api.answer = () => Promise.resolve(SUMMARY)
})

afterEach(() => {
  vi.useRealTimers()
})

describe('DashboardPage (HU012)', () => {
  it('shows the general indicators', async () => {
    renderPage()

    expect(
      await screen.findByText((1200).toLocaleString('es')),
    ).toBeInTheDocument()
    expect(screen.getByText('24')).toBeInTheDocument()
    const portfolio = screen.getByRole('region', { name: 'Portafolio' })
    expect(within(portfolio).getByText('Sistema ERP')).toBeInTheDocument()
    expect(
      within(portfolio).getByText(dateTime('2026-11-02T15:30:00Z')),
    ).toBeInTheDocument()
    expect(
      screen.getByText(dateTime('2026-11-04T21:15:00Z')),
    ).toBeInTheDocument()
  })

  it('shows the visit statistics (HU013)', async () => {
    renderPage()
    const visits = await screen.findByRole('region', { name: 'Visitas' })

    expect(within(visits).getByText('Visitas totales')).toBeInTheDocument()
    expect(
      within(visits).getByText((12583).toLocaleString('es')),
    ).toBeInTheDocument()
    expect(within(visits).getByText('54')).toBeInTheDocument()
    expect(within(visits).getByText('812')).toBeInTheDocument()
    expect(
      within(visits).getAllByText('Agenda de citas')[0],
    ).toBeInTheDocument()
    expect(within(visits).getByText('320 visitas')).toBeInTheDocument()
  })

  it('shows the latest published project next to the portfolio data', async () => {
    renderPage()
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
        visits: {
          total: 0,
          today: 0,
          thisMonth: 0,
          mostVisitedProject: null,
          ranking: [],
        },
      })
    renderPage()

    expect(await screen.findAllByText('Ninguno todavía')).toHaveLength(3)
    expect(
      screen.getByText('Aún no hay visitas a los proyectos.'),
    ).toBeInTheDocument()
    expect(screen.getByText('Este es tu primer acceso')).toBeInTheDocument()
  })

  it('offers to retry when the indicators cannot be loaded', async () => {
    api.answer = () => Promise.reject(new Error('network'))
    const user = userEvent.setup()
    renderPage()

    expect(
      await screen.findByText('No se pudieron cargar los indicadores.'),
    ).toBeInTheDocument()

    api.answer = () => Promise.resolve(SUMMARY)
    await user.click(screen.getByRole('button', { name: 'Reintentar' }))
    expect(
      await screen.findByRole('region', { name: 'Portafolio' }),
    ).toHaveTextContent('Sistema ERP')
  })

  it('ranks the most visited projects with a link to edit them (HU014)', async () => {
    renderPage()
    const ranking = await screen.findByRole('table', {
      name: 'Top 2 de proyectos por visitas a su detalle',
    })
    const rows = within(ranking).getAllByRole('row').slice(1)

    expect(rows.map((row) => row.textContent)).toEqual([
      '1Agenda de citas320',
      '2Sistema ERPOculto210',
    ])
    expect(
      within(rows[1]).getByRole('link', { name: 'Sistema ERP' }),
    ).toHaveAttribute('href', '/admin/proyectos/7/editar')
  })

  it('refreshes the figures every minute without hiding them', async () => {
    vi.useFakeTimers({ shouldAdvanceTime: true })
    renderPage()
    expect(await screen.findByText('320 visitas')).toBeInTheDocument()

    api.answer = () =>
      Promise.resolve({
        ...SUMMARY,
        visits: {
          ...SUMMARY.visits,
          mostVisitedProject: { name: 'Agenda de citas', visits: 321 },
        },
      })
    await act(async () => {
      vi.advanceTimersByTime(60_000)
    })

    expect(await screen.findByText('321 visitas')).toBeInTheDocument()
    expect(screen.queryByText('Cargando indicadores…')).not.toBeInTheDocument()
  })

  it('keeps the last figures when a refresh fails', async () => {
    vi.useFakeTimers({ shouldAdvanceTime: true })
    renderPage()
    expect(await screen.findByText('320 visitas')).toBeInTheDocument()

    api.answer = () => Promise.reject(new Error('network'))
    await act(async () => {
      vi.advanceTimersByTime(60_000)
    })

    expect(await screen.findByText(/No se pudo actualizar/)).toBeInTheDocument()
    expect(screen.getByText('320 visitas')).toBeInTheDocument()
  })
})
