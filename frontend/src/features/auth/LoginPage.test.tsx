import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { AxiosError, type AxiosResponse } from 'axios'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { fakeJwt } from '../../test/fakeJwt'
import type { LoginCredentials, TokenResponse } from './authApi'
import { AuthProvider } from './AuthProvider'
import { LoginPage } from './LoginPage'
import { ProtectedRoute } from './ProtectedRoute'

/*
 * A plain function instead of vi.fn(): vi.fn() chains onto the promise it returns to record the
 * result, and that chained copy is reported as an unhandled rejection when the login fails.
 */
const authApi = vi.hoisted(() => ({
  calls: [] as LoginCredentials[],
  respond: (): Promise<TokenResponse> => Promise.reject(new Error('not set')),
}))

vi.mock('./authApi', () => ({
  login: (credentials: LoginCredentials) => {
    authApi.calls.push(credentials)
    return authApi.respond()
  },
}))

function respondWith(roles: string[]) {
  authApi.respond = async () => ({
    accessToken: fakeJwt({
      sub: '1',
      email: 'admin@example.com',
      name: 'Admin',
      roles,
      exp: 9999999999,
    }),
    tokenType: 'Bearer',
    expiresAt: '2099-01-01T00:00:00Z',
  })
}

function failWith(error: Error) {
  authApi.respond = () => Promise.reject(error)
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

function renderApp(path = '/login') {
  render(
    <AuthProvider>
      <MemoryRouter initialEntries={[path]}>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route
            path="/admin"
            element={
              <ProtectedRoute>
                <p>Panel admin</p>
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/projects"
            element={
              <ProtectedRoute>
                <p>Proyectos</p>
              </ProtectedRoute>
            }
          />
        </Routes>
      </MemoryRouter>
    </AuthProvider>,
  )
}

async function fillAndSubmit(email: string, password: string) {
  const user = userEvent.setup()
  if (email) await user.type(screen.getByLabelText('Correo electrónico'), email)
  if (password) await user.type(screen.getByLabelText('Contraseña'), password)
  await user.click(screen.getByRole('button', { name: 'Ingresar' }))
}

beforeEach(() => {
  authApi.calls = []
  failWith(new Error('login response not configured'))
})

describe('LoginPage', () => {
  it('requires email and password', async () => {
    renderApp()

    await fillAndSubmit('', '')

    expect(
      await screen.findByText('El correo es obligatorio.'),
    ).toBeInTheDocument()
    expect(
      screen.getByText('La contraseña es obligatoria.'),
    ).toBeInTheDocument()
    expect(authApi.calls).toHaveLength(0)
  })

  it('validates the email format and the password policy (RF004)', async () => {
    renderApp()

    await fillAndSubmit('not-an-email', 'weak')

    expect(
      await screen.findByText('Ingresa un correo válido.'),
    ).toBeInTheDocument()
    expect(screen.getByText(/entre 6 y 12 caracteres/)).toBeInTheDocument()
    expect(screen.getByLabelText('Correo electrónico')).toHaveAttribute(
      'aria-invalid',
      'true',
    )
    expect(screen.getByLabelText('Contraseña')).toHaveAttribute(
      'aria-invalid',
      'true',
    )
    expect(authApi.calls).toHaveLength(0)
  })

  it('logs in and opens the admin panel', async () => {
    respondWith(['ADMIN'])
    renderApp()

    await fillAndSubmit('  admin@example.com ', 'Secret#123')

    expect(await screen.findByText('Panel admin')).toBeInTheDocument()
    expect(authApi.calls).toEqual([
      { email: 'admin@example.com', password: 'Secret#123' },
    ])
  })

  it('shows an error for wrong credentials', async () => {
    failWith(httpError(401))
    renderApp()

    await fillAndSubmit('admin@example.com', 'Wrong#123')

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Correo o contraseña incorrectos.',
    )
  })

  it('rejects a user without the ADMIN role', async () => {
    respondWith(['USER'])
    renderApp()

    await fillAndSubmit('user@example.com', 'Secret#123')

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Tu cuenta no tiene permisos de administrador.',
    )
    expect(screen.queryByText('Panel admin')).not.toBeInTheDocument()
  })

  it('shows a generic error when the server is unreachable', async () => {
    failWith(new AxiosError('Network Error', 'ERR_NETWORK'))
    renderApp()

    await fillAndSubmit('admin@example.com', 'Secret#123')

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'No se pudo conectar con el servidor.',
    )
  })
})

describe('ProtectedRoute', () => {
  it('redirects anonymous visitors to the login page', () => {
    renderApp('/admin')

    expect(
      screen.getByRole('heading', { name: 'Iniciar sesión' }),
    ).toBeInTheDocument()
    expect(screen.queryByText('Panel admin')).not.toBeInTheDocument()
  })

  it('returns to the originally requested page after logging in', async () => {
    respondWith(['ADMIN'])
    renderApp('/admin/projects')

    await fillAndSubmit('admin@example.com', 'Secret#123')

    expect(await screen.findByText('Proyectos')).toBeInTheDocument()
  })
})
