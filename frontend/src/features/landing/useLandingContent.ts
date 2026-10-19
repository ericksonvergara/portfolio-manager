import { isAxiosError } from 'axios'
import { useCallback, useEffect, useState } from 'react'
import {
  getProfile,
  getProjects,
  getTechnologies,
  type Profile,
  type ProjectCard,
  type Technology,
} from './landingApi'

export type LandingContent =
  | { status: 'loading' }
  | { status: 'error'; retry: () => void }
  | {
      status: 'ready'
      /** Null while the owner has not set up the profile yet (the API answers 404). */
      profile: Profile | null
      technologies: Technology[]
      projects: ProjectCard[]
    }

async function loadProfile(): Promise<Profile | null> {
  try {
    return await getProfile()
  } catch (error) {
    if (isAxiosError(error) && error.response?.status === 404) {
      return null
    }
    throw error
  }
}

export function useLandingContent(): LandingContent {
  const [attempt, setAttempt] = useState(0)
  const [content, setContent] = useState<LandingContent>({ status: 'loading' })

  const retry = useCallback(() => {
    setContent({ status: 'loading' })
    setAttempt((current) => current + 1)
  }, [])

  useEffect(() => {
    let active = true
    Promise.all([loadProfile(), getTechnologies(), getProjects()])
      .then(([profile, technologies, projects]) => {
        if (active) {
          setContent({ status: 'ready', profile, technologies, projects })
        }
      })
      .catch(() => {
        if (active) {
          setContent({ status: 'error', retry })
        }
      })
    return () => {
      active = false
    }
  }, [attempt, retry])

  return content
}
