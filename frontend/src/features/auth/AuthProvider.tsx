import { useCallback, useMemo, useState, type ReactNode } from 'react'
import { setAccessToken } from '../../services/api'
import {
  login as loginRequest,
  type LoginCredentials,
  type TokenResponse,
} from './authApi'
import {
  ADMIN_ROLE,
  AuthContext,
  NotAdminError,
  type AuthContextValue,
  type AuthSession,
} from './authContext'
import { decodeAccessToken } from './jwt'

function toSession(response: TokenResponse): AuthSession {
  const claims = decodeAccessToken(response.accessToken)
  return {
    accessToken: response.accessToken,
    expiresAt: new Date(response.expiresAt),
    user: {
      id: claims.sub,
      email: claims.email,
      name: claims.name,
      roles: claims.roles,
    },
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<AuthSession | null>(null)

  const login = useCallback(async (credentials: LoginCredentials) => {
    const newSession = toSession(await loginRequest(credentials))

    // The admin panel is the only private area: a session without the ADMIN role is not kept.
    if (!newSession.user.roles.includes(ADMIN_ROLE)) {
      throw new NotAdminError()
    }

    setAccessToken(newSession.accessToken)
    setSession(newSession)
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({
      session,
      isAuthenticated: session !== null,
      isAdmin: session?.user.roles.includes(ADMIN_ROLE) ?? false,
      login,
    }),
    [session, login],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
