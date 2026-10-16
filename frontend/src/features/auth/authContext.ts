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
  user: AuthUser
}

export interface AuthContextValue {
  session: AuthSession | null
  isAuthenticated: boolean
  isAdmin: boolean
  login: (credentials: LoginCredentials) => Promise<void>
}

export const AuthContext = createContext<AuthContextValue | null>(null)
