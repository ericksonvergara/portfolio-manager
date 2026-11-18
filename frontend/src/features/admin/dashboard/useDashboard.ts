import { useCallback, useEffect, useState } from 'react'
import { getDashboard, type DashboardSummary } from './adminDashboardApi'

/** How often the dashboard refreshes itself while it is on screen (HU014). */
export const REFRESH_INTERVAL_MS = 60_000

export type DashboardState =
  | { status: 'loading' }
  | { status: 'error'; retry: () => void }
  | {
      status: 'ready'
      data: DashboardSummary
      updatedAt: Date
      /** The last automatic refresh failed: the figures shown are the previous ones. */
      stale: boolean
    }

/**
 * Loads the dashboard and keeps it up to date: every minute, and when the tab becomes visible again.
 * A refresh keeps the current figures on screen, and a hidden tab asks for nothing.
 */
export function useDashboard(): DashboardState {
  const [attempt, setAttempt] = useState(0)
  const [state, setState] = useState<DashboardState>({ status: 'loading' })

  const retry = useCallback(() => {
    setState({ status: 'loading' })
    setAttempt((current) => current + 1)
  }, [])

  useEffect(() => {
    let active = true

    const load = () => {
      getDashboard()
        .then((data) => {
          if (active) {
            setState({
              status: 'ready',
              data,
              updatedAt: new Date(),
              stale: false,
            })
          }
        })
        .catch(() => {
          if (!active) return
          setState((current) =>
            current.status === 'ready'
              ? { ...current, stale: true }
              : { status: 'error', retry },
          )
        })
    }

    const refreshIfVisible = () => {
      if (document.visibilityState === 'visible') load()
    }

    load()
    const timer = window.setInterval(refreshIfVisible, REFRESH_INTERVAL_MS)
    document.addEventListener('visibilitychange', refreshIfVisible)
    return () => {
      active = false
      window.clearInterval(timer)
      document.removeEventListener('visibilitychange', refreshIfVisible)
    }
  }, [attempt, retry])

  return state
}
