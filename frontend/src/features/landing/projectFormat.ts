const MONTH_YEAR = new Intl.DateTimeFormat('es', {
  month: 'long',
  year: 'numeric',
})

/**
 * Formats an ISO date (YYYY-MM-DD) as "octubre de 2026". It is built from its parts instead of
 * `new Date(iso)`, which reads it as UTC midnight and shows the previous day west of Greenwich.
 */
export function formatProjectDate(isoDate: string): string {
  const [year, month, day] = isoDate.split('-').map(Number)
  return MONTH_YEAR.format(new Date(year, month - 1, day))
}

const YOUTUBE_ID = /^[\w-]{11}$/

/**
 * Embeddable URL for a YouTube video (privacy-enhanced domain), or null for any other link,
 * which the page shows as a plain link instead.
 */
export function toVideoEmbedUrl(videoUrl: string): string | null {
  let url: URL
  try {
    url = new URL(videoUrl)
  } catch {
    return null
  }

  const host = url.hostname.replace(/^www\./, '')
  let id: string | null = null
  if (host === 'youtu.be') {
    id = url.pathname.slice(1)
  } else if (host === 'youtube.com' || host === 'm.youtube.com') {
    id = url.pathname.startsWith('/embed/')
      ? url.pathname.slice('/embed/'.length)
      : url.searchParams.get('v')
  }

  return id && YOUTUBE_ID.test(id)
    ? `https://www.youtube-nocookie.com/embed/${id}`
    : null
}
