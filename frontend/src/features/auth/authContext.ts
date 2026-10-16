import { createContext } from 'react'
import type { LoginCredentials } from './authApi'

export const ADMIN_ROLE = 'ADMIN'

export class NotAdminError extends Error {
  constructor() {
    super('The authenticated user is not an administrator')
    this.name = 'NotAdminError'
  }
}

export interface AuthUser {
  id: string
  email: string
  name: string
  roles: string[]
}

export interface AuthSession {
  accessToken: string
  expiresAt: Date
  /** Epoch milliseconds at which the session is renewed in the background, if known. */
  renewAt: number | null
  user: AuthUser
}

export interface AuthContextValue {
  session: AuthSession | null
  isAuthenticated: boolean
  isAdmin: boolean
  /** True while the session is being restored from the refresh cookie after a page load. */
  isRestoring: boolean
  /** True when the backend stopped accepting the session and the user has to log in again. */
  sessionExpired: boolean
  login: (credentials: LoginCredentials) => Promise<void>
}

export const AuthContext = createContext<AuthContextValue | null>(null)
