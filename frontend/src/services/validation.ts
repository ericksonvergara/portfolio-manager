/** Same rule as the backend (EmailAddress): something before the @, a domain with a dot, and no spaces. */
export const EMAIL_PATTERN = /^[^@\s]+@[^@\s]+\.[^@\s.]+$/

/**
 * Same rule as the backend (WebUrl, RN002, RN003): an absolute http(s) URL with a domain that has a dot
 * (or an IP address), a real port and no user or password inside.
 */
export function isWebUrl(value: string): boolean {
  try {
    const url = new URL(value)
    return (
      (url.protocol === 'http:' || url.protocol === 'https:') &&
      (url.hostname.includes('.') || url.hostname.startsWith('[')) &&
      url.username === '' &&
      url.password === '' &&
      // The backend parses the raw text: it rejects what the browser would silently encode or fix.
      !/\s|%(?![0-9a-f]{2})/i.test(value) &&
      !url.hostname
        .split('.')
        .some((label) => label === '' || label.startsWith('-'))
    )
  } catch {
    return false
  }
}

/** The message shown next to an invalid link. */
export const WEB_URL_MESSAGE =
  'Ingresa una URL completa y válida, por ejemplo https://ejemplo.com.'
