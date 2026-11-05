import { useEffect, useRef } from 'react'
import { registerVisit } from './visitsApi'

/**
 * Registers one visit when a public page opens (HU025). It waits until `ready`, so a page that fails to
 * load is not counted. A failure is ignored: the statistics must never disturb the visitor. The ref
 * keeps React's development double effect from counting the same page twice.
 */
export function useRegisterVisit(projectSlug?: string, ready = true) {
  const registered = useRef<string | null>(null)

  useEffect(() => {
    const page = projectSlug ?? ''
    if (!ready || registered.current === page) return
    registered.current = page
    registerVisit(projectSlug).catch(() => {})
  }, [projectSlug, ready])
}
