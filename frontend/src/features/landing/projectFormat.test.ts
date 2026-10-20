import { describe, expect, it } from 'vitest'
import { formatProjectDate, toVideoEmbedUrl } from './projectFormat'

describe('formatProjectDate', () => {
  it('shows the month and year of the local date, whatever the time zone', () => {
    expect(formatProjectDate('2026-10-01')).toBe('octubre de 2026')
    expect(formatProjectDate('2025-01-31')).toBe('enero de 2025')
  })
})

describe('toVideoEmbedUrl', () => {
  it.each([
    'https://www.youtube.com/watch?v=dQw4w9WgXcQ',
    'https://youtube.com/watch?v=dQw4w9WgXcQ&t=30s',
    'https://youtu.be/dQw4w9WgXcQ',
    'https://www.youtube.com/embed/dQw4w9WgXcQ',
    'https://m.youtube.com/watch?v=dQw4w9WgXcQ',
  ])('embeds the YouTube link %s', (url) => {
    expect(toVideoEmbedUrl(url)).toBe(
      'https://www.youtube-nocookie.com/embed/dQw4w9WgXcQ',
    )
  })

  it.each([
    'https://vimeo.com/123456',
    'https://www.youtube.com/watch?v=<script>',
    'https://evil.example/youtube.com/watch?v=dQw4w9WgXcQ',
    'not a url',
  ])('does not embed %s', (url) => {
    expect(toVideoEmbedUrl(url)).toBeNull()
  })
})
