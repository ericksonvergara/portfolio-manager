import { AxiosError, type AxiosResponse } from 'axios'
import { describe, expect, it } from 'vitest'
import type { Profile } from '../../landing/landingApi'
import {
  EMAIL_PATTERN,
  EMPTY_PROFILE,
  fromProfile,
  toProfileFormError,
  toProfileInput,
} from './profileFormModel'

const PROFILE: Profile = {
  fullName: 'Erickson Vergara',
  headline: 'Desarrollador Full Stack',
  about: 'Presentación.',
  photoUrl: '/uploads/foto.png',
  email: 'contacto@example.com',
  location: null,
  socialLinks: [
    { platform: 'WEBSITE', url: 'https://example.com' },
    { platform: 'GITHUB', url: 'https://github.com/ericksonvergara' },
  ],
}

function httpError(status: number, message: string) {
  return new AxiosError(
    'Request failed',
    'ERR_BAD_REQUEST',
    undefined,
    undefined,
    {
      status,
      data: { message },
    } as AxiosResponse,
  )
}

describe('profileFormModel', () => {
  it('turns the profile into form values and back, in the order of the platforms', () => {
    const values = fromProfile(PROFILE)
    expect(values.location).toBe('')
    expect(values.links.GITHUB).toBe('https://github.com/ericksonvergara')
    expect(values.links.LINKEDIN).toBe('')

    expect(toProfileInput(values)).toEqual({
      fullName: 'Erickson Vergara',
      headline: 'Desarrollador Full Stack',
      about: 'Presentación.',
      email: 'contacto@example.com',
      location: null,
      socialLinks: [
        { platform: 'GITHUB', url: 'https://github.com/ericksonvergara' },
        { platform: 'WEBSITE', url: 'https://example.com' },
      ],
    })
  })

  it('trims the values and leaves out the empty links', () => {
    const input = toProfileInput({
      ...EMPTY_PROFILE,
      fullName: '  Erickson ',
      email: ' contacto@example.com ',
      location: '  Colombia ',
      links: {
        ...EMPTY_PROFILE.links,
        X: '  ',
        LINKEDIN: ' https://linkedin.com/in/e ',
      },
    })
    expect(input.fullName).toBe('Erickson')
    expect(input.email).toBe('contacto@example.com')
    expect(input.location).toBe('Colombia')
    expect(input.socialLinks).toEqual([
      { platform: 'LINKEDIN', url: 'https://linkedin.com/in/e' },
    ])
  })

  it('accepts the same emails as the backend', () => {
    expect(EMAIL_PATTERN.test('contacto@example.com')).toBe(true)
    expect(EMAIL_PATTERN.test('a.b+c@mail.example.co')).toBe(true)
    for (const invalid of [
      'contacto',
      'contacto@example',
      '@example.com',
      'a b@example.com',
    ]) {
      expect(EMAIL_PATTERN.test(invalid)).toBe(false)
    }
  })

  it('marks the email when the backend rejects it', () => {
    const error = toProfileFormError(
      httpError(400, 'email must be a valid email address'),
    )
    expect(error.field).toBe('email')
    expect(
      toProfileFormError(httpError(400, 'about must not be blank')).field,
    ).toBeUndefined()
    expect(
      toProfileFormError(
        httpError(400, 'GITHUB link must be a valid http or https URL'),
      ).field,
    ).toBe('links.GITHUB')
    expect(toProfileFormError(new Error('network')).message).toBe(
      'No se pudo guardar el perfil. Inténtalo de nuevo.',
    )
  })
})
