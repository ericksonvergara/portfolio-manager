import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { AxiosError, type AxiosResponse } from 'axios'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import type { ProfileInput } from '../../features/admin/profile/adminProfileApi'
import type { Profile } from '../../features/landing/landingApi'
import { ProfilePage } from './ProfilePage'

/* Plain functions instead of vi.fn(), for the same reason as in LoginPage.test.tsx. */
const api = vi.hoisted(() => ({
  profile: (): Promise<Profile | null> => Promise.reject(new Error('not set')),
  saved: [] as ProfileInput[],
  save: (): Promise<Profile> => Promise.reject(new Error('not set')),
  photos: [] as string[],
  photo: (): Promise<Profile> => Promise.reject(new Error('not set')),
}))

vi.mock('../../features/admin/profile/adminProfileApi', () => ({
  getAdminProfile: () => api.profile(),
  saveProfile: (input: ProfileInput) => {
    api.saved.push(input)
    return api.save()
  },
  uploadProfilePhoto: (file: File) => {
    api.photos.push(`upload ${file.name}`)
    return api.photo()
  },
  removeProfilePhoto: () => {
    api.photos.push('remove')
    return api.photo()
  },
}))

const PROFILE: Profile = {
  fullName: 'Erickson Vergara',
  headline: 'Desarrollador Full Stack',
  about: 'Presentación.',
  photoUrl: null,
  email: 'contacto@example.com',
  location: 'Colombia',
  socialLinks: [
    { platform: 'GITHUB', url: 'https://github.com/ericksonvergara' },
  ],
}

function httpError(status: number, message?: string) {
  return new AxiosError(
    'Request failed',
    'ERR_BAD_REQUEST',
    undefined,
    undefined,
    {
      status,
      data: { message },
    } as AxiosResponse,
  )
}

function png(name = 'foto.png') {
  return new File([new Uint8Array(10)], name, { type: 'image/png' })
}

beforeEach(() => {
  api.profile = () => Promise.resolve(PROFILE)
  api.saved = []
  api.save = () => Promise.reject(new Error('not set'))
  api.photos = []
  api.photo = () => Promise.reject(new Error('not set'))
})

describe('ProfilePage', () => {
  it('loads the profile into the form', async () => {
    render(<ProfilePage />)

    expect(await screen.findByLabelText('Nombre')).toHaveValue(
      'Erickson Vergara',
    )
    expect(screen.getByLabelText('Correo de contacto')).toHaveValue(
      'contacto@example.com',
    )
    expect(screen.getByLabelText(/Ubicación/)).toHaveValue('Colombia')
    expect(screen.getByLabelText('GitHub')).toHaveValue(
      'https://github.com/ericksonvergara',
    )
    expect(screen.getByLabelText('LinkedIn')).toHaveValue('')
    expect(screen.getByText(/Sin fotografía/)).toBeInTheDocument()
  })

  it('saves the name, the email and the links', async () => {
    api.save = () =>
      Promise.resolve({ ...PROFILE, fullName: 'Erickson Vergara Rojas' })
    const user = userEvent.setup()
    render(<ProfilePage />)
    const name = await screen.findByLabelText('Nombre')
    await user.clear(name)
    await user.type(name, 'Erickson Vergara Rojas')
    const email = screen.getByLabelText('Correo de contacto')
    await user.clear(email)
    await user.type(email, 'nuevo@example.com')
    await user.clear(screen.getByLabelText('GitHub'))
    await user.type(
      screen.getByLabelText('LinkedIn'),
      'https://linkedin.com/in/erickson',
    )
    await user.click(screen.getByRole('button', { name: 'Guardar perfil' }))

    expect(await screen.findByText('Perfil guardado.')).toBeInTheDocument()
    expect(api.saved).toEqual([
      {
        fullName: 'Erickson Vergara Rojas',
        headline: 'Desarrollador Full Stack',
        about: 'Presentación.',
        email: 'nuevo@example.com',
        location: 'Colombia',
        socialLinks: [
          { platform: 'LINKEDIN', url: 'https://linkedin.com/in/erickson' },
        ],
      },
    ])
  })

  it('validates the email format and the links before saving', async () => {
    const user = userEvent.setup()
    render(<ProfilePage />)
    const email = await screen.findByLabelText('Correo de contacto')
    await user.clear(email)
    await user.type(email, 'contacto@example')
    await user.type(screen.getByLabelText('Sitio web'), 'example.com')
    await user.clear(screen.getByLabelText('Nombre'))
    await user.click(screen.getByRole('button', { name: 'Guardar perfil' }))

    expect(
      await screen.findByText(/Ingresa un correo válido/),
    ).toBeInTheDocument()
    expect(email).toHaveAttribute('aria-invalid', 'true')
    expect(screen.getByText('El nombre es obligatorio.')).toBeInTheDocument()
    expect(screen.getByText(/Ingresa una URL válida/)).toBeInTheDocument()
    expect(api.saved).toEqual([])
  })

  it('marks the email when the backend rejects it', async () => {
    api.save = () =>
      Promise.reject(
        httpError(400, 'email: must be a well-formed email address'),
      )
    const user = userEvent.setup()
    render(<ProfilePage />)
    await user.click(
      await screen.findByRole('button', { name: 'Guardar perfil' }),
    )

    expect(
      await screen.findByText(/Ingresa un correo válido/),
    ).toBeInTheDocument()
    expect(screen.getByLabelText('Correo de contacto')).toHaveAttribute(
      'aria-invalid',
      'true',
    )
  })

  it('creates the profile when there is none, and only then allows a photo', async () => {
    api.profile = () => Promise.resolve(null)
    api.save = () => Promise.resolve(PROFILE)
    const user = userEvent.setup()
    render(<ProfilePage />)

    expect(await screen.findByText(/Aún no hay un perfil/)).toBeInTheDocument()
    expect(screen.getByLabelText('Nombre')).toHaveValue('')
    expect(
      screen.queryByLabelText(/Archivo de la fotografía/),
    ).not.toBeInTheDocument()

    await user.type(screen.getByLabelText('Nombre'), 'Erickson Vergara')
    await user.type(
      screen.getByLabelText('Titular profesional'),
      'Desarrollador Full Stack',
    )
    await user.type(screen.getByLabelText('Sobre mí'), 'Presentación.')
    await user.type(
      screen.getByLabelText('Correo de contacto'),
      'contacto@example.com',
    )
    await user.click(screen.getByRole('button', { name: 'Guardar perfil' }))

    expect(await screen.findByText('Perfil guardado.')).toBeInTheDocument()
    expect(api.saved[0].socialLinks).toEqual([])
    expect(
      screen.getByLabelText(/Archivo de la fotografía/),
    ).toBeInTheDocument()
  })

  it('uploads a previewed photo and removes it after confirming', async () => {
    const user = userEvent.setup()
    render(<ProfilePage />)
    await user.upload(
      await screen.findByLabelText(/Archivo de la fotografía/),
      png(),
    )

    expect(
      screen.getByAltText('Vista previa de la fotografía'),
    ).toBeInTheDocument()
    api.photo = () =>
      Promise.resolve({ ...PROFILE, photoUrl: '/uploads/foto.png' })
    await user.click(screen.getByRole('button', { name: 'Subir fotografía' }))

    expect(await screen.findByText('Fotografía guardada.')).toBeInTheDocument()
    expect(
      screen.getByAltText('Fotografía de Erickson Vergara'),
    ).toHaveAttribute('src', '/uploads/foto.png')

    api.photo = () => Promise.resolve(PROFILE)
    await user.click(screen.getByRole('button', { name: 'Quitar fotografía' }))
    await user.click(screen.getByRole('button', { name: 'Quitar' }))

    expect(await screen.findByText('Fotografía eliminada.')).toBeInTheDocument()
    expect(api.photos).toEqual(['upload foto.png', 'remove'])
  })

  it('rejects a file that is not an image without uploading it', async () => {
    const user = userEvent.setup({ applyAccept: false })
    render(<ProfilePage />)
    await user.upload(
      await screen.findByLabelText(/Archivo de la fotografía/),
      new File(['x'], 'foto.gif', { type: 'image/gif' }),
    )

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Formato no permitido',
    )
    expect(
      screen.getByRole('button', { name: 'Subir fotografía' }),
    ).toBeDisabled()
    expect(api.photos).toEqual([])
  })

  it('offers to retry when the profile cannot be loaded', async () => {
    api.profile = () => Promise.reject(httpError(500))
    render(<ProfilePage />)

    expect(
      await screen.findByText('No se pudo cargar el perfil.'),
    ).toBeInTheDocument()
    expect(
      screen.getByRole('button', { name: 'Reintentar' }),
    ).toBeInTheDocument()
  })
})
