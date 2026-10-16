import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { SessionRestoring } from './SessionRestoring'
import { useAuth } from './useAuth'

/**
 * Only administrators get through; anyone else is sent to the login page, remembering where they
 * were going so the login can bring them back.
 */
export function ProtectedRoute({ children }: { children: ReactNode }) {
  const { isAuthenticated, isAdmin, isRestoring } = useAuth()
  const location = useLocation()

  if (isRestoring) {
    return <SessionRestoring />
  }

  if (!isAuthenticated || !isAdmin) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }

  return children
}
