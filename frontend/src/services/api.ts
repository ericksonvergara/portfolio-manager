import axios from 'axios'

let accessToken: string | null = null

/**
 * The access token lives only in memory (never in localStorage) so an XSS payload cannot read it
 * from storage. The refresh token travels in an HttpOnly cookie managed by the browser.
 */
export function setAccessToken(token: string | null) {
  accessToken = token
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
