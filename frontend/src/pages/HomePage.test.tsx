import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { AxiosError, type AxiosResponse } from 'axios'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import type {
  Profile,
  ProjectCard,
  Technology,
} from '../features/landing/landingApi'
import { HomePage } from './HomePage'

/* Plain functions instead of vi.fn(), for the same reason as in LoginPage.test.tsx. */
const landingApi = vi.hoisted(() => ({
  profile: (): Promise<Profile> => Promise.reject(new Error('not set')),
  technologies: (): Promise<Technology[]> => Promise.resolve([]),
  projects: (): Promise<ProjectCard[]> => Promise.resolve([]),
}))

vi.mock('../features/landing/landingApi', () => ({
  getProfile: () => landingApi.profile(),
  getTechnologies: () => landingApi.technologies(),
  getProjects: () => landingApi.projects(),
}))

function renderHome() {
  render(
    <MemoryRouter>
      <HomePage />
    </MemoryRouter>,
  )
}

const PROJECTS: ProjectCard[] = [
  {
    slug: 'portfolio-manager',
    name: 'Portfolio Manager',
    shortDescription: 'Portafolio con panel administrativo seguro.',
    coverImageUrl: '/demo/projects/portfolio-manager.svg',
    projectUrl: 'https://example.com',
    repositoryUrl: 'https://github.com/ericksonvergara/portfolio-manager',
    featured: true,
    technologies: ['Java', 'Spring Boot', 'React'],
  },
  {
    slug: 'agenda-de-citas',
    name: 'Agenda de Citas',
    shortDescription: 'Reserva de citas en línea.',
    coverImageUrl: '/demo/projects/agenda-de-citas.svg',
    projectUrl: null,
    repositoryUrl: null,
    featured: false,
    technologies: [],
  },
]

const PROFILE: Profile = {
  fullName: 'Erickson Vergara',
  headline: 'Desarrollador Full Stack',
  about: 'Construyo aplicaciones web de extremo a extremo.',
  photoUrl: '/demo/profile.svg',
  email: 'contacto@example.com',
  location: 'Colombia',
  socialLinks: [
    { platform: 'GITHUB', url: 'https://github.com/ericksonvergara' },
    { platform: 'LINKEDIN', url: 'https://www.linkedin.com/in/example' },
  ],
}

const TECHNOLOGIES: Technology[] = [
  { name: 'Java', category: 'BACKEND' },
  { name: 'React', category: 'FRONTEND' },
  { name: 'Spring Boot', category: 'BACKEND' },
]

function httpError(status: number) {
  return new AxiosError(
    'Request failed',
    'ERR_BAD_REQUEST',
    undefined,
    undefined,
    {
      status,
    } as AxiosResponse,
  )
}

beforeEach(() => {
  landingApi.profile = () => Promise.resolve(PROFILE)
  landingApi.technologies = () => Promise.resolve(TECHNOLOGIES)
  landingApi.projects = () => Promise.resolve(PROJECTS)
})

describe('HomePage projects (HU002)', () => {
  it('shows a card for every published project', async () => {
    renderHome()

    const portfolio = await screen.findByRole('region', { name: 'Portafolio' })
    const cards = within(portfolio).getAllByRole('article')
    expect(cards).toHaveLength(2)

    const first = within(cards[0])
    expect(
      first.getByRole('heading', { name: 'Portfolio Manager' }),
    ).toBeInTheDocument()
    expect(
      first.getByText('Portafolio con panel administrativo seguro.'),
    ).toBeInTheDocument()
    expect(first.getByText('Destacado')).toBeInTheDocument()
    expect(
      within(
        first.getByRole('list', { name: 'Tecnologías de Portfolio Manager' }),
      )
        .getAllByRole('listitem')
        .map((li) => li.textContent),
    ).toEqual(['Java', 'Spring Boot', 'React'])
    expect(
      within(portfolio).getByRole('img', {
        name: 'Imagen del proyecto Portfolio Manager',
      }),
    ).toHaveAttribute('src', '/demo/projects/portfolio-manager.svg')
  })

  it('links each card to its detail page and its repository', async () => {
    renderHome()

    const portfolio = await screen.findByRole('region', { name: 'Portafolio' })
    expect(
      within(portfolio).getByRole('link', {
        name: 'Ver proyecto Portfolio Manager',
      }),
    ).toHaveAttribute('href', '/proyectos/portfolio-manager')

    const repository = within(portfolio).getByRole('link', {
      name: 'Repositorio de Portfolio Manager',
    })
    expect(repository).toHaveAttribute(
      'href',
      'https://github.com/ericksonvergara/portfolio-manager',
    )
    expect(repository).toHaveAttribute('target', '_blank')
  })

  it('hides the repository button when the project has none', async () => {
    renderHome()

    const portfolio = await screen.findByRole('region', { name: 'Portafolio' })
    expect(
      within(portfolio).getByRole('link', {
        name: 'Ver proyecto Agenda de Citas',
      }),
    ).toBeInTheDocument()
    expect(
      within(portfolio).queryByRole('link', {
        name: 'Repositorio de Agenda de Citas',
      }),
    ).not.toBeInTheDocument()
  })

  it('shows an empty state when nothing is published yet', async () => {
    landingApi.projects = () => Promise.resolve([])
    renderHome()

    const portfolio = await screen.findByRole('region', { name: 'Portafolio' })
    expect(
      within(portfolio).getByText('Los proyectos publicados aparecerán aquí.'),
    ).toBeInTheDocument()
    expect(within(portfolio).queryByRole('article')).not.toBeInTheDocument()
  })
})

describe('HomePage (HU001)', () => {
  it('shows the personal presentation and photo without logging in', async () => {
    renderHome()

    expect(screen.getByRole('status')).toHaveTextContent(
      'Cargando portafolio...',
    )
    expect(
      await screen.findByRole('heading', {
        level: 1,
        name: 'Erickson Vergara',
      }),
    ).toBeInTheDocument()
    expect(screen.getByText('Desarrollador Full Stack')).toBeInTheDocument()
    expect(
      screen.getByRole('img', { name: 'Fotografía de Erickson Vergara' }),
    ).toHaveAttribute('src', '/demo/profile.svg')
    expect(
      screen.getByText('Construyo aplicaciones web de extremo a extremo.'),
    ).toBeInTheDocument()
  })

  it('groups the technologies by category', async () => {
    renderHome()

    const section = await screen.findByRole('region', { name: 'Tecnologías' })
    const backend = within(section)
      .getByRole('heading', { name: 'Backend' })
      .closest('div')!
    expect(
      within(backend)
        .getAllByRole('listitem')
        .map((li) => li.textContent),
    ).toEqual(['Java', 'Spring Boot'])
    expect(within(section).getByText('React')).toBeInTheDocument()
    expect(
      within(section).queryByRole('heading', { name: 'Móvil' }),
    ).not.toBeInTheDocument()
  })

  it('links the social networks and the contact email', async () => {
    renderHome()

    const contact = await screen.findByRole('region', { name: 'Contacto' })
    expect(
      within(contact).getByRole('link', { name: 'contacto@example.com' }),
    ).toHaveAttribute('href', 'mailto:contacto@example.com')

    const github = within(contact).getByRole('link', { name: 'GitHub' })
    expect(github).toHaveAttribute('href', 'https://github.com/ericksonvergara')
    expect(github).toHaveAttribute('target', '_blank')
    expect(github).toHaveAttribute('rel', 'noopener noreferrer')
    expect(
      within(contact).getByRole('link', { name: 'LinkedIn' }),
    ).toBeInTheDocument()
  })

  it('offers navigation to every section', async () => {
    renderHome()

    const nav = await screen.findByRole('navigation', { name: 'Secciones' })
    expect(
      within(nav)
        .getAllByRole('link')
        .map((link) => link.getAttribute('href')),
    ).toEqual(['#sobre-mi', '#tecnologias', '#portafolio', '#contacto'])
  })

  it('shows a placeholder while the profile is not set up', async () => {
    landingApi.profile = () => Promise.reject(httpError(404))
    renderHome()

    expect(
      await screen.findByRole('heading', {
        name: 'Portafolio en construcción',
      }),
    ).toBeInTheDocument()
    expect(
      screen.queryByRole('region', { name: 'Contacto' }),
    ).not.toBeInTheDocument()
    expect(
      screen.getByRole('region', { name: 'Tecnologías' }),
    ).toBeInTheDocument()
  })

  it('lets the visitor retry when the server is unreachable', async () => {
    landingApi.profile = () =>
      Promise.reject(new AxiosError('Network Error', 'ERR_NETWORK'))
    renderHome()

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'No se pudo cargar el portafolio.',
    )

    landingApi.profile = () => Promise.resolve(PROFILE)
    await userEvent
      .setup()
      .click(screen.getByRole('button', { name: 'Reintentar' }))

    expect(
      await screen.findByRole('heading', {
        level: 1,
        name: 'Erickson Vergara',
      }),
    ).toBeInTheDocument()
  })
})
