import { isAxiosError } from 'axios'
import {
  useCallback,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { setAccessToken, setSessionRenewer } from '../../services/api'
import {
  login as loginRequest,
  logout as logoutRequest,
  refresh as refreshRequest,
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
import { announceLogout, onLogoutInAnotherTab } from './sessionSync'
import { renewalDelayMs } from './sessionTiming'

function toSession(response: TokenResponse): AuthSession {
  const claims = decodeAccessToken(response.accessToken)
  const delay = renewalDelayMs(claims)
  return {
    accessToken: response.accessToken,
    expiresAt: new Date(response.expiresAt),
    renewAt: delay === null ? null : Date.now() + delay,
    user: {
      id: claims.sub,
      email: claims.email,
      name: claims.name,
      roles: claims.roles,
    },
  }
}

/** A network error or a server failure says nothing about the session, so the session is kept. */
function isTransientFailure(error: unknown): boolean {
  return (
    isAxiosError(error) && (!error.response || error.response.status >= 500)
  )
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<AuthSession | null>(null)
  const [isRestoring, setIsRestoring] = useState(true)
  const [sessionExpired, setSessionExpired] = useState(false)

  const startSession = useCallback((response: TokenResponse) => {
    const newSession = toSession(response)

    // The admin panel is the only private area: a session without the ADMIN role is not kept.
    if (!newSession.user.roles.includes(ADMIN_ROLE)) {
      throw new NotAdminError()
    }

    setAccessToken(newSession.accessToken)
    setSession(newSession)
    setSessionExpired(false)
    return newSession
  }, [])

  const expireSession = useCallback(() => {
    setAccessToken(null)
    setSession(null)
    setSessionExpired(true)
  }, [])

  const renewSession = useCallback(async (): Promise<string | null> => {
    try {
      return startSession(await refreshRequest()).accessToken
    } catch (error) {
      if (!isTransientFailure(error)) {
        expireSession()
      }
      return null
    }
  }, [startSession, expireSession])

  const login = useCallback(
    async (credentials: LoginCredentials) => {
      startSession(await loginRequest(credentials))
    },
    [startSession],
  )

  const clearSession = useCallback(() => {
    setAccessToken(null)
    setSession(null)
    setSessionExpired(false)
  }, [])

  const logout = useCallback(async () => {
    // Dropping the session first stops the background renewal and leaves the protected routes at
    // once; the access token stays set until the request, which needs it to revoke it, is sent.
    setSession(null)
    setSessionExpired(false)
    try {
      await logoutRequest()
    } catch {
      // The backend could not be reached: the session still ends in this browser.
    } finally {
      setAccessToken(null)
      announceLogout()
    }
  }, [])

  useEffect(() => onLogoutInAnotherTab(clearSession), [clearSession])

  // The access token is gone after a reload; the refresh cookie survives it and restores the session.
  useEffect(() => {
    refreshRequest()
      .then(startSession)
      // Without a valid refresh cookie the visitor simply starts logged out.
      .catch(() => undefined)
      .finally(() => setIsRestoring(false))
  }, [startSession])

  useEffect(() => {
    setSessionRenewer(renewSession)
    return () => setSessionRenewer(null)
  }, [renewSession])

  // Renewing shortly before the access token expires keeps the session alive without a failed request.
  useEffect(() => {
    if (session?.renewAt == null) {
      return
    }
    const timer = window.setTimeout(
      () => void renewSession(),
      Math.max(0, session.renewAt - Date.now()),
    )
    return () => window.clearTimeout(timer)
  }, [session, renewSession])

  const value = useMemo<AuthContextValue>(
    () => ({
      session,
      isAuthenticated: session !== null,
      isAdmin: session?.user.roles.includes(ADMIN_ROLE) ?? false,
      isRestoring,
      sessionExpired,
      login,
      logout,
    }),
    [session, isRestoring, sessionExpired, login, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
