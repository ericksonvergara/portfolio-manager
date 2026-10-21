import { isAxiosError } from 'axios'
import { useCallback, useEffect, useState } from 'react'

export type AdminData<T> =
  | { status: 'loading' }
  | { status: 'not-found' }
  | { status: 'error'; retry: () => void }
  | { status: 'ready'; data: T }

/** Loads data for an admin page, with a retry after a failure; a 404 is reported apart. */
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
      .catch((error: unknown) => {
        if (!active) return
        const notFound = isAxiosError(error) && error.response?.status === 404
        setState(
          notFound ? { status: 'not-found' } : { status: 'error', retry },
        )
      })
    return () => {
      active = false
    }
  }, [load, attempt, retry])

  return state
}
