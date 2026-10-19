import { render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import App from './App'

vi.mock('./features/landing/landingApi', () => ({
  getProfile: () =>
    Promise.resolve({
      fullName: 'Erickson Vergara',
      headline: 'Desarrollador Full Stack',
      about: 'Presentación',
      photoUrl: null,
      email: 'contacto@example.com',
      location: null,
      socialLinks: [],
    }),
  getTechnologies: () => Promise.resolve([]),
  getProjects: () => Promise.resolve([]),
}))

describe('App', () => {
  it('opens on the public landing page', async () => {
    render(<App />)

    expect(
      await screen.findByRole('heading', {
        level: 1,
        name: 'Erickson Vergara',
      }),
    ).toBeInTheDocument()
  })
})
