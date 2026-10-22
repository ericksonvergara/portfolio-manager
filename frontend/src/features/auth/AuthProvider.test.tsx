import { act, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { AxiosError, type AxiosAdapter, type AxiosResponse } from 'axios'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { api, setAccessToken } from '../../services/api'
import { fakeJwt } from '../../test/fakeJwt'
import type { TokenResponse } from './authApi'
import { AuthProvider } from './AuthProvider'
import { LoginPage } from './LoginPage'
import { AdminLayout } from '../admin/AdminLayout'
import { ProtectedRoute } from './ProtectedRoute'

/*
 * Each refresh takes the next queued answer; with none left the cookie is gone (401). A plain
 * function instead of vi.fn() for the same reason as in LoginPage.test.tsx.
 */
const authApi = vi.hoisted(() => ({
  refreshCalls: 0,
  refreshAnswers: [] as Array<() => Promise<TokenResponse>>,
  logoutCalls: [] as Array<string | null>,
  logoutFails: false,
}))

vi.mock('./authApi', async () => {
  const { api } = await import('../../services/api')
  return {
    login: () => Promise.reject(new Error('login is not used in these tests')),
    refresh: () => {
      authApi.refreshCalls += 1
      const answer = authApi.refreshAnswers.shift()
      return answer ? answer() : Promise.reject(httpError(401))
    },
    // Goes through the real client so the test sees the Authorization header the backend gets.
    logout: async () => {
      await api.post('/api/auth/logout', undefined, {
        adapter: async (config) => {
          authApi.logoutCalls.push(
            (config.headers.Authorization as string | undefined) ?? null,
          )
          if (authApi.logoutFails) {
            throw new AxiosError('Network Error', 'ERR_NETWORK', config)
          }
          return { data: '', status: 204, statusText: '', headers: {}, config }
        },
      })
    },
  }
})

let tokenCount = 0

function session({
  roles = ['ADMIN'],
  lifetimeSeconds = 900,
}: { roles?: string[]; lifetimeSeconds?: number } = {}): TokenResponse {
  tokenCount += 1
  const iat = Math.floor(Date.now() / 1000)
  const exp = iat + lifetimeSeconds
  return {
    accessToken: fakeJwt({
      sub: '1',
      email: 'admin@example.com',
      name: 'Admin',
      roles,
      iat,
      exp,
      jti: String(tokenCount),
    }),
    tokenType: 'Bearer',
    expiresAt: new Date(exp * 1000).toISOString(),
  }
}

function refreshReturns(...answers: Array<TokenResponse | Error>) {
  authApi.refreshAnswers = answers.map((answer) =>
    answer instanceof Error
      ? () => Promise.reject(answer)
      : () => Promise.resolve(answer),
  )
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

function renderApp(path = '/admin') {
  render(
    <AuthProvider>
      <MemoryRouter initialEntries={[path]}>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route
            path="/admin"
            element={
              <ProtectedRoute>
                <AdminLayout />
              </ProtectedRoute>
            }
          />
        </Routes>
      </MemoryRouter>
    </AuthProvider>,
  )
}

beforeEach(() => {
  authApi.refreshCalls = 0
  authApi.refreshAnswers = []
  authApi.logoutCalls = []
  authApi.logoutFails = false
})

afterEach(() => setAccessToken(null))

describe('AuthProvider', () => {
  it('restores the session after a page reload', async () => {
    refreshReturns(session())
    renderApp()

    expect(screen.getByRole('status')).toHaveTextContent(
      'Restaurando sesión...',
    )
    expect(await screen.findByText('Panel administrativo')).toBeInTheDocument()
  })

  it('shows the login page when there is no session to restore', async () => {
    renderApp()

    expect(
      await screen.findByRole('heading', { name: 'Iniciar sesión' }),
    ).toBeInTheDocument()
    expect(screen.queryByText(/sesión expiró/)).not.toBeInTheDocument()
  })

  it('does not restore a session without the ADMIN role', async () => {
    refreshReturns(session({ roles: ['USER'] }))
    renderApp()

    expect(
      await screen.findByRole('heading', { name: 'Iniciar sesión' }),
    ).toBeInTheDocument()
  })

  it('renews the session before the access token expires', async () => {
    // A 1 second token is renewed after half a second.
    refreshReturns(session({ lifetimeSeconds: 1 }), session())
    renderApp()

    expect(await screen.findByText('Panel administrativo')).toBeInTheDocument()
    await waitFor(() => expect(authApi.refreshCalls).toBe(2), {
      timeout: 2000,
    })
    expect(screen.getByText('Panel administrativo')).toBeInTheDocument()
  })

  it('ends the session when the backend no longer accepts it', async () => {
    refreshReturns(session({ lifetimeSeconds: 1 }), httpError(401))
    renderApp()

    expect(await screen.findByText('Panel administrativo')).toBeInTheDocument()
    expect(
      await screen.findByText(
        'Tu sesión expiró. Vuelve a iniciar sesión.',
        {},
        {
          timeout: 2000,
        },
      ),
    ).toBeInTheDocument()
    expect(screen.queryByText('Panel administrativo')).not.toBeInTheDocument()
  })

  it('keeps the session when the renewal fails for a network error', async () => {
    refreshReturns(
      session({ lifetimeSeconds: 1 }),
      new AxiosError('Network Error', 'ERR_NETWORK'),
    )
    renderApp()

    expect(await screen.findByText('Panel administrativo')).toBeInTheDocument()
    await waitFor(() => expect(authApi.refreshCalls).toBe(2), {
      timeout: 2000,
    })
    expect(screen.getByText('Panel administrativo')).toBeInTheDocument()
  })

  it('retries a request rejected with 401 using the renewed token', async () => {
    const restored = session()
    const renewed = session()
    refreshReturns(restored, renewed)
    renderApp()
    await screen.findByText('Panel administrativo')

    const adapter: AxiosAdapter = async (config) => {
      if (config.headers.Authorization === `Bearer ${restored.accessToken}`) {
        throw new AxiosError(
          'Unauthorized',
          'ERR_BAD_REQUEST',
          config,
          undefined,
          { status: 401, config } as AxiosResponse,
        )
      }
      return {
        data: config.headers.Authorization,
        status: 200,
        statusText: 'OK',
        headers: {},
        config,
      }
    }

    const response = await act(() => api.get('/api/projects', { adapter }))

    expect(response.data).toBe(`Bearer ${renewed.accessToken}`)
    expect(authApi.refreshCalls).toBe(2)
  })
})

describe('logout (HU009)', () => {
  async function logOut() {
    await userEvent
      .setup()
      .click(await screen.findByRole('button', { name: 'Cerrar sesión' }))
  }

  it('revokes the session on the backend and returns to the login page', async () => {
    const restored = session()
    refreshReturns(restored)
    renderApp()

    await logOut()

    expect(
      await screen.findByRole('heading', { name: 'Iniciar sesión' }),
    ).toBeInTheDocument()
    expect(authApi.logoutCalls).toEqual([`Bearer ${restored.accessToken}`])
    expect(screen.queryByText(/sesión expiró/)).not.toBeInTheDocument()
  })

  it('drops the access token once the session is closed', async () => {
    refreshReturns(session())
    renderApp()
    await logOut()
    await screen.findByRole('heading', { name: 'Iniciar sesión' })

    const response = await api.get('/api/anything', {
      adapter: async (config) => ({
        data: config.headers.Authorization ?? null,
        status: 200,
        statusText: 'OK',
        headers: {},
        config,
      }),
    })

    expect(response.data).toBeNull()
  })

  it('ends the session in the browser even if the backend is unreachable', async () => {
    authApi.logoutFails = true
    refreshReturns(session())
    renderApp()

    await logOut()

    expect(
      await screen.findByRole('heading', { name: 'Iniciar sesión' }),
    ).toBeInTheDocument()
    expect(authApi.logoutCalls).toHaveLength(1)
  })

  it('ends the session when another tab logs out', async () => {
    refreshReturns(session())
    renderApp()
    await screen.findByText('Panel administrativo')

    const otherTab = new BroadcastChannel('portfolio-manager:auth')
    otherTab.postMessage('logout')
    otherTab.close()

    expect(
      await screen.findByRole('heading', { name: 'Iniciar sesión' }),
    ).toBeInTheDocument()
    expect(authApi.logoutCalls).toHaveLength(0)
  })
})
