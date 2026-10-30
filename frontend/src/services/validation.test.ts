import { describe, expect, it } from 'vitest'
import { EMAIL_PATTERN, isWebUrl } from './validation'

describe('isWebUrl (RN002, RN003)', () => {
  it.each([
    'https://example.com',
    'http://example.com',
    'https://github.com/ericksonvergara/portfolio-manager?tab=readme#top',
    'https://www.youtube.com/watch?v=abc123',
    'http://192.168.0.1:8080/demo',
    'https://example.com/a%20b',
  ])('accepts %s', (url) => {
    expect(isWebUrl(url)).toBe(true)
  })

  it.each([
    'javascript:alert(1)',
    'ftp://example.com',
    'www.example.com',
    'https://',
    'https://localhost',
    'https://foo',
    'https://user:pass@example.com',
    'https://github.com@evil.example',
    'https://example.com:99999',
    'https://example.com/a b',
    'https://example..com',
    'https://-example.com',
    'https://example.com/%zz',
  ])('rejects %s, like the backend', (url) => {
    expect(isWebUrl(url)).toBe(false)
  })
})

describe('EMAIL_PATTERN', () => {
  it('follows the backend rule', () => {
    expect(EMAIL_PATTERN.test('ana@example.com')).toBe(true)
    expect(EMAIL_PATTERN.test('ana@example')).toBe(false)
  })
})
