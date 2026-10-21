import { useCallback, useEffect, useState } from 'react'

export type AdminData<T> =
  | { status: 'loading' }
  | { status: 'error'; retry: () => void }
  | { status: 'ready'; data: T }

/** Loads data for an admin page, with a retry after a failure. */
export function useAdminData<T>(load: () => Promise<T>): AdminData<T> {
  const [attempt, setAttempt] = useState(0)
  const [state, setState] = useState<AdminData<T>>({ status: 'loading' })

  const retry = useCallback(() => {
    setState({ status: 'loading' })
    setAttempt((current) => current + 1)
  }, [])

  useEffect(() => {
    let active = true
    load()
      .then((data) => {
        if (active) setState({ status: 'ready', data })
      })
      .catch(() => {
        if (active) setState({ status: 'error', retry })
      })
    return () => {
      active = false
    }
  }, [load, attempt, retry])

  return state
}
