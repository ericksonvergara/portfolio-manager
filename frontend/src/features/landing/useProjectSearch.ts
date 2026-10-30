import { useEffect, useState } from 'react'
import {
  getProjects,
  type ProjectCard,
  type ProjectCategory,
} from './landingApi'

/** Same limit as the backend (ProjectSearch.MAX_LENGTH). */
export const MAX_SEARCH_LENGTH = 100

/** Waits for the visitor to stop typing before asking the server. */
const DEBOUNCE_MS = 300

export type ProjectSearchState =
  | { status: 'idle'; projects: ProjectCard[] }
  | { status: 'searching'; projects: ProjectCard[] }
  | { status: 'error'; projects: ProjectCard[] }

/**
 * Searches the published projects by name or technology (HU004) and filters them by category (HU005).
 * Without search nor category it shows the projects already loaded with the page, without a request;
 * while a request is running, the previous results stay visible, and an answer that arrives after a
 * newer one was asked for is ignored.
 */
export function useProjectSearch(
  all: ProjectCard[],
  query: string,
  category: ProjectCategory | null,
): ProjectSearchState {
  const text = query.trim()
  const key = text || category ? `${category ?? ''}|${text}` : ''
  const [result, setResult] = useState<{
    key: string
    projects: ProjectCard[]
    failed: boolean
  } | null>(null)

  useEffect(() => {
    if (!key) return
    let current = true
    // A click on a filter answers at once; typing waits for a pause.
    const delay = text ? DEBOUNCE_MS : 0
    const timer = setTimeout(() => {
      getProjects({ search: text, category })
        .then((projects) => {
          if (current) setResult({ key, projects, failed: false })
        })
        .catch(() => {
          if (current) setResult({ key, projects: [], failed: true })
        })
    }, delay)
    return () => {
      current = false
      clearTimeout(timer)
    }
  }, [key, text, category])

  if (!key) return { status: 'idle', projects: all }
  if (result?.key !== key) {
    return { status: 'searching', projects: result?.projects ?? all }
  }
  return result.failed
    ? { status: 'error', projects: [] }
    : { status: 'idle', projects: result.projects }
}
