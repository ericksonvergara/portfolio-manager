import { isAxiosError } from 'axios'
import type { Profile, SocialPlatform } from '../../landing/landingApi'
import { SOCIAL_LABELS } from '../../landing/labels'
import { WEB_URL_MESSAGE } from '../../../services/validation'
import type { ProfileInput } from './adminProfileApi'

/** The platforms in the order the form shows them, which is also the order of the landing page. */
export const PLATFORMS = Object.keys(SOCIAL_LABELS) as SocialPlatform[]

export { EMAIL_PATTERN } from '../../../services/validation'

export interface ProfileFormValues {
  fullName: string
  headline: string
  about: string
  email: string
  location: string
  /** One URL per platform; an empty one means the platform is not shown. */
  links: Record<SocialPlatform, string>
}

const NO_LINKS = Object.fromEntries(PLATFORMS.map((p) => [p, ''])) as Record<
  SocialPlatform,
  string
>

export const EMPTY_PROFILE: ProfileFormValues = {
  fullName: '',
  headline: '',
  about: '',
  email: '',
  location: '',
  links: NO_LINKS,
}

export function fromProfile(profile: Profile): ProfileFormValues {
  const links = { ...NO_LINKS }
  for (const link of profile.socialLinks) links[link.platform] = link.url
  return {
    fullName: profile.fullName,
    headline: profile.headline,
    about: profile.about,
    email: profile.email,
    location: profile.location ?? '',
    links,
  }
}

export function toProfileInput(values: ProfileFormValues): ProfileInput {
  return {
    fullName: values.fullName.trim(),
    headline: values.headline.trim(),
    about: values.about.trim(),
    email: values.email.trim(),
    location: values.location.trim() || null,
    socialLinks: PLATFORMS.filter((p) => values.links[p].trim() !== '').map(
      (platform) => ({ platform, url: values.links[platform].trim() }),
    ),
  }
}

/** The form rejects the submission with a field error; anything else is shown above the form. */
/** The fields a server rejection can point to. */
export type ProfileErrorField = 'email' | `links.${SocialPlatform}`

export class ProfileFormError extends Error {
  readonly field?: ProfileErrorField

  constructor(message: string, field?: ProfileErrorField) {
    super(message)
    this.field = field
  }
}

/** Turns a failed save into the message the form shows. */
export function toProfileFormError(error: unknown): ProfileFormError {
  const status = isAxiosError(error) ? error.response?.status : undefined
  const message = isAxiosError<{ message?: string }>(error)
    ? (error.response?.data?.message ?? '')
    : ''
  if (status === 400 && message.startsWith('email')) {
    return new ProfileFormError(
      'Ingresa un correo válido, por ejemplo contacto@dominio.com.',
      'email',
    )
  }
  const invalidLink =
    status === 400 ? /^([A-Z]+) link must be a valid/.exec(message) : null
  if (invalidLink && PLATFORMS.includes(invalidLink[1] as SocialPlatform)) {
    return new ProfileFormError(
      WEB_URL_MESSAGE,
      `links.${invalidLink[1] as SocialPlatform}`,
    )
  }
  if (status === 400) {
    return new ProfileFormError('Revisa los datos ingresados.')
  }
  return new ProfileFormError(
    'No se pudo guardar el perfil. Inténtalo de nuevo.',
  )
}
