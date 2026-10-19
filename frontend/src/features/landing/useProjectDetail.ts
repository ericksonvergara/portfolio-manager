import { isAxiosError } from 'axios'
import { useCallback, useEffect, useState } from 'react'
import { getProject, type ProjectDetail } from './landingApi'

export type ProjectDetailContent =
  | { status: 'loading' }
  /** No published project has that slug; inactive projects answer the same way. */
  | { status: 'not-found' }
  | { status: 'error'; retry: () => void }
  | { status: 'ready'; project: ProjectDetail }

export function useProjectDetail(slug: string): ProjectDetailContent {
  const [attempt, setAttempt] = useState(0)
  const [loaded, setLoaded] = useState<{
    slug: string
    content: ProjectDetailContent
  } | null>(null)

  const retry = useCallback(() => {
    setLoaded(null)
    setAttempt((current) => current + 1)
  }, [])

  useEffect(() => {
    let active = true
    getProject(slug)
      .then((project) => {
        if (active) {
          setLoaded({ slug, content: { status: 'ready', project } })
        }
      })
      .catch((error: unknown) => {
        if (!active) {
          return
        }
        const notFound = isAxiosError(error) && error.response?.status === 404
        setLoaded({
          slug,
          content: notFound
            ? { status: 'not-found' }
            : { status: 'error', retry },
        })
      })
    return () => {
      active = false
    }
  }, [slug, attempt, retry])

  // A result for another slug is stale: the page is loading the new project.
  return loaded?.slug === slug ? loaded.content : { status: 'loading' }
}
