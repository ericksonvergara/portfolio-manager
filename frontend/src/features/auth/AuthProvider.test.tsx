import { act, render, screen, waitFor } from '@testing-library/react'
import { AxiosError, type AxiosAdapter, type AxiosResponse } from 'axios'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { api, setAccessToken } from '../../services/api'
import { fakeJwt } from '../../test/fakeJwt'
import type { TokenResponse } from './authApi'
import { AuthProvider } from './AuthProvider'
import { LoginPage } from './LoginPage'
import { ProtectedRoute } from './ProtectedRoute'

/*
 * Each refresh takes the next queued answer; with none left the cookie is gone (401). A plain
 * function instead of vi.fn() for the same reason as in LoginPage.test.tsx.
 */
const authApi = vi.hoisted(() => ({
  refreshCalls: 0,
  refreshAnswers: [] as Array<() => Promise<TokenResponse>>,
}))

vi.mock('./authApi', () => ({
  login: () => Promise.reject(new Error('login is not used in these tests')),
  refresh: () => {
    authApi.refreshCalls += 1
    const answer = authApi.refreshAnswers.shift()
    return answer ? answer() : Promise.reject(httpError(401))
  },
}))

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
                <p>Panel admin</p>
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
})

afterEach(() => setAccessToken(null))

describe('AuthProvider', () => {
  it('restores the session after a page reload', async () => {
    refreshReturns(session())
    renderApp()

    expect(screen.getByRole('status')).toHaveTextContent(
      'Restaurando sesión...',
    )
    expect(await screen.findByText('Panel admin')).toBeInTheDocument()
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

    expect(await screen.findByText('Panel admin')).toBeInTheDocument()
    await waitFor(() => expect(authApi.refreshCalls).toBe(2), {
      timeout: 2000,
    })
    expect(screen.getByText('Panel admin')).toBeInTheDocument()
  })

  it('ends the session when the backend no longer accepts it', async () => {
    refreshReturns(session({ lifetimeSeconds: 1 }), httpError(401))
    renderApp()

    expect(await screen.findByText('Panel admin')).toBeInTheDocument()
    expect(
      await screen.findByText(
        'Tu sesión expiró. Vuelve a iniciar sesión.',
        {},
        {
          timeout: 2000,
        },
      ),
    ).toBeInTheDocument()
    expect(screen.queryByText('Panel admin')).not.toBeInTheDocument()
  })

  it('keeps the session when the renewal fails for a network error', async () => {
    refreshReturns(
      session({ lifetimeSeconds: 1 }),
      new AxiosError('Network Error', 'ERR_NETWORK'),
    )
    renderApp()

    expect(await screen.findByText('Panel admin')).toBeInTheDocument()
    await waitFor(() => expect(authApi.refreshCalls).toBe(2), {
      timeout: 2000,
    })
    expect(screen.getByText('Panel admin')).toBeInTheDocument()
  })

  it('retries a request rejected with 401 using the renewed token', async () => {
    const restored = session()
    const renewed = session()
    refreshReturns(restored, renewed)
    renderApp()
    await screen.findByText('Panel admin')

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
