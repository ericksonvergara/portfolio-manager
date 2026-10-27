import { useEffect, useState } from 'react'
import { getProjects, type ProjectCard } from './landingApi'

/** Same limit as the backend (ProjectSearch.MAX_LENGTH). */
export const MAX_SEARCH_LENGTH = 100

/** Waits for the visitor to stop typing before asking the server. */
const DEBOUNCE_MS = 300

export type ProjectSearchState =
  | { status: 'idle'; projects: ProjectCard[] }
  | { status: 'searching'; projects: ProjectCard[] }
  | { status: 'error'; projects: ProjectCard[] }

/**
 * Searches the published projects by name or technology (HU004). An empty search shows the projects
 * already loaded with the page, without a request; while a search is running, the previous results
 * stay visible, and an answer that arrives after a newer search is ignored.
 */
export function useProjectSearch(
  all: ProjectCard[],
  query: string,
): ProjectSearchState {
  const text = query.trim()
  const [result, setResult] = useState<{
    text: string
    projects: ProjectCard[]
    failed: boolean
  } | null>(null)

  useEffect(() => {
    if (!text) return
    let current = true
    const timer = setTimeout(() => {
      getProjects(text)
        .then((projects) => {
          if (current) setResult({ text, projects, failed: false })
        })
        .catch(() => {
          if (current) setResult({ text, projects: [], failed: true })
        })
    }, DEBOUNCE_MS)
    return () => {
      current = false
      clearTimeout(timer)
    }
  }, [text])

  if (!text) return { status: 'idle', projects: all }
  if (result?.text !== text) {
    return { status: 'searching', projects: result?.projects ?? all }
  }
  return result.failed
    ? { status: 'error', projects: [] }
    : { status: 'idle', projects: result.projects }
}
