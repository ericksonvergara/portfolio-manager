import { api } from '../../services/api'

export interface LoginCredentials {
  email: string
  password: string
}

export interface TokenResponse {
  accessToken: string
  tokenType: string
  expiresAt: string
}

const REFRESH_LOCK = 'portfolio-manager:auth-refresh'

let refreshInFlight: Promise<TokenResponse> | null = null

export async function login(
  credentials: LoginCredentials,
): Promise<TokenResponse> {
  const { data } = await api.post<TokenResponse>('/api/auth/login', credentials)
  return data
}

/**
 * Exchanges the refresh cookie for a new access token. The backend rotates the refresh token on
 * every call and, if it sees a rotated one again, treats it as stolen and closes every session of
 * the user. So concurrent callers share one request, and tabs of the same browser take turns.
 */
export function refresh(): Promise<TokenResponse> {
  refreshInFlight ??= withRefreshLock(async () => {
    const { data } = await api.post<TokenResponse>('/api/auth/refresh')
    return data
  }).finally(() => {
    refreshInFlight = null
  })
  return refreshInFlight
}

/**
 * Revokes the session on the backend: the refresh token in the cookie, which the response clears,
 * and the access token sent as Bearer. It runs after any refresh in progress, so a refresh cannot
 * hand out a new cookie once the session is closed.
 */
export async function logout(): Promise<void> {
  await refreshInFlight?.catch(() => undefined)
  await withRefreshLock(() => api.post('/api/auth/logout'))
}

function withRefreshLock<T>(task: () => Promise<T>): Promise<T> {
  return 'locks' in navigator
    ? navigator.locks.request(REFRESH_LOCK, task)
    : task()
}
