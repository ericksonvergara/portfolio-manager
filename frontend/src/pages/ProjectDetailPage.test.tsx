import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { AxiosError, type AxiosResponse } from 'axios'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import type { ProjectDetail } from '../features/landing/landingApi'
import { ProjectDetailPage } from './ProjectDetailPage'

/* Plain functions instead of vi.fn(), for the same reason as in LoginPage.test.tsx. */
const landingApi = vi.hoisted(() => ({
  requested: [] as string[],
  project: (): Promise<ProjectDetail> => Promise.reject(new Error('not set')),
}))

vi.mock('../features/landing/landingApi', () => ({
  getProject: (slug: string) => {
    landingApi.requested.push(slug)
    return landingApi.project()
  },
}))

const PROJECT: ProjectDetail = {
  slug: 'portfolio-manager',
  name: 'Portfolio Manager',
  description: 'Plataforma para publicar y administrar proyectos de software.',
  coverImageUrl: '/demo/projects/portfolio-manager.svg',
  projectUrl: 'https://example.com',
  repositoryUrl: 'https://github.com/ericksonvergara/portfolio-manager',
  videoUrl: 'https://www.youtube.com/watch?v=dQw4w9WgXcQ',
  status: 'IN_PROGRESS',
  developedOn: '2026-10-01',
  gallery: [
    { url: '/demo/projects/one.svg', altText: 'Landing Page del portafolio' },
    { url: '/demo/projects/two.svg', altText: null },
  ],
  technologies: ['Java', 'Spring Boot', 'React'],
}

function httpError(status: number) {
  return new AxiosError(
    'Request failed',
    'ERR_BAD_REQUEST',
    undefined,
    undefined,
    { status } as AxiosResponse,
  )
}

function renderDetail(slug = 'portfolio-manager') {
  render(
    <MemoryRouter initialEntries={[`/proyectos/${slug}`]}>
      <Routes>
        <Route path="/proyectos/:slug" element={<ProjectDetailPage />} />
        <Route path="/" element={<p>Landing</p>} />
      </Routes>
    </MemoryRouter>,
  )
}

beforeEach(() => {
  landingApi.requested = []
  landingApi.project = () => Promise.resolve(PROJECT)
  // jsdom does not implement scrolling.
  vi.spyOn(window, 'scrollTo').mockImplementation(() => {})
})

describe('ProjectDetailPage (HU003)', () => {
  it('shows the name, full description, status, date and technologies', async () => {
    renderDetail()

    expect(screen.getByRole('status')).toHaveTextContent('Cargando proyecto...')
    expect(
      await screen.findByRole('heading', {
        level: 1,
        name: 'Portfolio Manager',
      }),
    ).toBeInTheDocument()
    expect(landingApi.requested).toEqual(['portfolio-manager'])
    expect(
      screen.getByText(
        'Plataforma para publicar y administrar proyectos de software.',
      ),
    ).toBeInTheDocument()
    expect(screen.getByText('En desarrollo')).toBeInTheDocument()
    expect(screen.getByText('octubre de 2026')).toBeInTheDocument()
    expect(
      within(
        screen.getByRole('list', { name: 'Tecnologías de Portfolio Manager' }),
      )
        .getAllByRole('listitem')
        .map((li) => li.textContent),
    ).toEqual(['Java', 'Spring Boot', 'React'])
  })

  it('links the live project and the repository', async () => {
    renderDetail()

    const live = await screen.findByRole('link', {
      name: 'Ver proyecto en línea',
    })
    expect(live).toHaveAttribute('href', 'https://example.com')
    expect(live).toHaveAttribute('target', '_blank')
    expect(screen.getByRole('link', { name: 'Repositorio' })).toHaveAttribute(
      'href',
      'https://github.com/ericksonvergara/portfolio-manager',
    )
  })

  it('shows the gallery with alternative texts', async () => {
    renderDetail()

    const gallery = await screen.findByRole('region', { name: 'Galería' })
    expect(
      within(gallery)
        .getAllByRole('img')
        .map((img) => img.getAttribute('alt')),
    ).toEqual([
      'Landing Page del portafolio',
      'Imagen 2 de la galería de Portfolio Manager',
    ])
  })

  it('embeds the YouTube video', async () => {
    renderDetail()

    const video = await screen.findByTitle(
      'Video demostrativo de Portfolio Manager',
    )
    expect(video).toHaveAttribute(
      'src',
      'https://www.youtube-nocookie.com/embed/dQw4w9WgXcQ',
    )
  })

  it('leaves out the optional parts the project does not have', async () => {
    landingApi.project = () =>
      Promise.resolve({
        ...PROJECT,
        projectUrl: null,
        repositoryUrl: null,
        videoUrl: null,
        developedOn: null,
        gallery: [],
        status: 'COMPLETED',
      })
    renderDetail()

    expect(await screen.findByText('Finalizado')).toBeInTheDocument()
    expect(screen.queryByText('Fecha:')).not.toBeInTheDocument()
    expect(
      screen.queryByRole('region', { name: 'Galería' }),
    ).not.toBeInTheDocument()
    expect(
      screen.queryByRole('region', { name: 'Video' }),
    ).not.toBeInTheDocument()
    expect(
      screen.queryByRole('link', { name: 'Repositorio' }),
    ).not.toBeInTheDocument()
  })

  it('tells the visitor when the project does not exist or is unpublished', async () => {
    landingApi.project = () => Promise.reject(httpError(404))
    renderDetail('prototipo-oculto')

    expect(
      await screen.findByRole('heading', { name: 'Proyecto no encontrado' }),
    ).toBeInTheDocument()
  })

  it('lets the visitor retry after a network error and go back to the portfolio', async () => {
    const user = userEvent.setup()
    landingApi.project = () =>
      Promise.reject(new AxiosError('Network Error', 'ERR_NETWORK'))
    renderDetail()

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'No se pudo cargar el proyecto.',
    )
    landingApi.project = () => Promise.resolve(PROJECT)
    await user.click(screen.getByRole('button', { name: 'Reintentar' }))
    expect(
      await screen.findByRole('heading', {
        level: 1,
        name: 'Portfolio Manager',
      }),
    ).toBeInTheDocument()

    await user.click(
      screen.getByRole('link', { name: '← Volver al portafolio' }),
    )
    expect(screen.getByText('Landing')).toBeInTheDocument()
  })
})
