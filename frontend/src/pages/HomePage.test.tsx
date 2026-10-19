import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { AxiosError, type AxiosResponse } from 'axios'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import type { Profile, Technology } from '../features/landing/landingApi'
import { HomePage } from './HomePage'

/* Plain functions instead of vi.fn(), for the same reason as in LoginPage.test.tsx. */
const landingApi = vi.hoisted(() => ({
  profile: (): Promise<Profile> => Promise.reject(new Error('not set')),
  technologies: (): Promise<Technology[]> => Promise.resolve([]),
}))

vi.mock('../features/landing/landingApi', () => ({
  getProfile: () => landingApi.profile(),
  getTechnologies: () => landingApi.technologies(),
}))

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
})

describe('HomePage (HU001)', () => {
  it('shows the personal presentation and photo without logging in', async () => {
    render(<HomePage />)

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
    render(<HomePage />)

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
    render(<HomePage />)

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
    render(<HomePage />)

    const nav = await screen.findByRole('navigation', { name: 'Secciones' })
    expect(
      within(nav)
        .getAllByRole('link')
        .map((link) => link.getAttribute('href')),
    ).toEqual(['#sobre-mi', '#tecnologias', '#portafolio', '#contacto'])
  })

  it('shows a placeholder while the profile is not set up', async () => {
    landingApi.profile = () => Promise.reject(httpError(404))
    render(<HomePage />)

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
    render(<HomePage />)

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
