import axios, { isAxiosError, type InternalAxiosRequestConfig } from 'axios'

let accessToken: string | null = null

/** Renews the session and returns the new access token, or null when it can no longer be renewed. */
export type SessionRenewer = () => Promise<string | null>

let renewSession: SessionRenewer | null = null

/**
 * The access token lives only in memory (never in localStorage) so an XSS payload cannot read it
 * from storage. The refresh token travels in an HttpOnly cookie managed by the browser.
 */
export function setAccessToken(token: string | null) {
  accessToken = token
}

/** Registered by the AuthProvider so a request rejected with 401 can renew the session and retry. */
export function setSessionRenewer(renewer: SessionRenewer | null) {
  renewSession = renewer
}

export const api = axios.create({
  // Empty by default: requests go to the same origin (nginx in Docker, the Vite proxy in development).
  baseURL: import.meta.env.VITE_API_URL ?? '',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
})

api.interceptors.request.use((config) => {
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`
  }
  return config
})

interface RetriableRequestConfig extends InternalAxiosRequestConfig {
  sessionRenewed?: boolean
}

api.interceptors.response.use(undefined, async (error: unknown) => {
  if (
    !isAxiosError(error) ||
    error.response?.status !== 401 ||
    !error.config ||
    !renewSession
  ) {
    throw error
  }

  const config: RetriableRequestConfig = error.config
  // A 401 from the auth endpoints is the answer itself, and a request is retried only once.
  if (config.sessionRenewed || config.url?.startsWith('/api/auth/')) {
    throw error
  }

  const newToken = await renewSession()
  if (!newToken) {
    throw error
  }

  config.sessionRenewed = true
  return api.request(config)
})
