import type { SocialPlatform, TechnologyCategory } from './landingApi'

export const SOCIAL_LABELS: Record<SocialPlatform, string> = {
  GITHUB: 'GitHub',
  LINKEDIN: 'LinkedIn',
  X: 'X',
  INSTAGRAM: 'Instagram',
  YOUTUBE: 'YouTube',
  WEBSITE: 'Sitio web',
}

/** Category order on the page, with the heading shown for each one. */
export const CATEGORY_LABELS: Record<TechnologyCategory, string> = {
  BACKEND: 'Backend',
  FRONTEND: 'Frontend',
  DATABASE: 'Bases de datos',
  DEVOPS: 'DevOps',
  MOBILE: 'Móvil',
  TOOLS: 'Herramientas',
}
