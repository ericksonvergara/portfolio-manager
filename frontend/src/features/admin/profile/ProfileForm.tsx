import { useForm, useWatch } from 'react-hook-form'
import { SOCIAL_LABELS } from '../../landing/labels'
import { isWebUrl, WEB_URL_MESSAGE } from '../../../services/validation'
import type { ProfileInput } from './adminProfileApi'
import {
  EMAIL_PATTERN,
  PLATFORMS,
  ProfileFormError,
  type ProfileFormValues,
  toProfileInput,
} from './profileFormModel'

const INPUT_CLASS =
  'w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-white outline-none focus:border-sky-500 focus:ring-2 focus:ring-sky-500/40 aria-[invalid=true]:border-red-500'

type TextField = Exclude<keyof ProfileFormValues, 'links'>

interface ProfileFormProps {
  defaultValues: ProfileFormValues
  onSubmit: (input: ProfileInput) => Promise<void>
}

/** The text data of the public profile (HU023); the photo has its own section. */
export function ProfileForm({ defaultValues, onSubmit }: ProfileFormProps) {
  const {
    register,
    handleSubmit,
    control,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<ProfileFormValues>({ mode: 'onTouched', defaultValues })
  const about = useWatch({ control, name: 'about' })

  const submit = async (values: ProfileFormValues) => {
    try {
      await onSubmit(toProfileInput(values))
    } catch (error) {
      const known =
        error instanceof ProfileFormError
          ? error
          : new ProfileFormError(
              'No se pudo guardar el perfil. Inténtalo de nuevo.',
            )
      setError(known.field ?? 'root', { message: known.message })
    }
  }

  const required = (label: string, max: number) => ({
    validate: (value: string) =>
      value.trim() !== '' || `${label} es obligatorio.`,
    maxLength: { value: max, message: `Máximo ${max} caracteres.` },
  })

  const a11y = (name: TextField) => ({
    'aria-invalid': errors[name] ? ('true' as const) : ('false' as const),
    'aria-describedby': errors[name] ? `${name}-error` : undefined,
  })

  const fieldError = (name: TextField) =>
    errors[name] ? (
      <p id={`${name}-error`} className="mt-1 text-sm text-red-400">
        {errors[name]?.message}
      </p>
    ) : null

  const label = (htmlFor: string, text: string, optional = false) => (
    <label
      htmlFor={htmlFor}
      className="mb-1 block text-sm font-medium text-slate-200"
    >
      {text}
      {optional && <span className="text-slate-500"> (opcional)</span>}
    </label>
  )

  return (
    <form
      noValidate
      onSubmit={handleSubmit(submit)}
      className="max-w-3xl space-y-6"
    >
      {errors.root && (
        <p
          role="alert"
          className="rounded-lg border border-red-800 bg-red-950 px-3 py-2 text-sm text-red-300"
        >
          {errors.root.message}
        </p>
      )}

      <div className="grid gap-6 sm:grid-cols-2">
        <div>
          {label('fullName', 'Nombre')}
          <input
            id="fullName"
            autoComplete="name"
            className={INPUT_CLASS}
            {...a11y('fullName')}
            {...register('fullName', required('El nombre', 100))}
          />
          {fieldError('fullName')}
        </div>
        <div>
          {label('headline', 'Titular profesional')}
          <input
            id="headline"
            placeholder="Desarrollador Full Stack"
            className={INPUT_CLASS}
            {...a11y('headline')}
            {...register('headline', required('El titular', 150))}
          />
          {fieldError('headline')}
        </div>
      </div>

      <div>
        {label('about', 'Sobre mí')}
        <textarea
          id="about"
          rows={5}
          className={INPUT_CLASS}
          {...a11y('about')}
          {...register('about', required('El texto "Sobre mí"', 2000))}
        />
        <p className="mt-1 text-xs text-slate-500">{about.length}/2000</p>
        {fieldError('about')}
      </div>

      <div className="grid gap-6 sm:grid-cols-2">
        <div>
          {label('email', 'Correo de contacto')}
          <input
            id="email"
            type="email"
            autoComplete="email"
            className={INPUT_CLASS}
            {...a11y('email')}
            {...register('email', {
              ...required('El correo', 150),
              pattern: {
                value: EMAIL_PATTERN,
                message:
                  'Ingresa un correo válido, por ejemplo contacto@dominio.com.',
              },
            })}
          />
          <p className="mt-1 text-xs text-slate-500">
            Se muestra en el portafolio. No cambia el correo con el que inicias
            sesión.
          </p>
          {fieldError('email')}
        </div>
        <div>
          {label('location', 'Ubicación', true)}
          <input
            id="location"
            placeholder="Bogotá, Colombia"
            className={INPUT_CLASS}
            {...a11y('location')}
            {...register('location', {
              maxLength: { value: 100, message: 'Máximo 100 caracteres.' },
            })}
          />
          {fieldError('location')}
        </div>
      </div>

      <fieldset>
        <legend className="mb-1 text-sm font-medium text-slate-200">
          Redes sociales
        </legend>
        <p className="mb-3 text-xs text-slate-500">
          Deja vacía una red para no mostrarla.
        </p>
        <div className="grid gap-4 sm:grid-cols-2">
          {PLATFORMS.map((platform) => {
            const error = errors.links?.[platform]
            const id = `link-${platform}`
            return (
              <div key={platform}>
                <label
                  htmlFor={id}
                  className="mb-1 block text-sm text-slate-300"
                >
                  {SOCIAL_LABELS[platform]}
                </label>
                <input
                  id={id}
                  type="url"
                  placeholder="https://"
                  className={INPUT_CLASS}
                  aria-invalid={error ? 'true' : 'false'}
                  aria-describedby={error ? `${id}-error` : undefined}
                  {...register(`links.${platform}`, {
                    validate: (value) =>
                      !value.trim() ||
                      isWebUrl(value.trim()) ||
                      WEB_URL_MESSAGE,
                    maxLength: {
                      value: 500,
                      message: 'Máximo 500 caracteres.',
                    },
                  })}
                />
                {error && (
                  <p id={`${id}-error`} className="mt-1 text-sm text-red-400">
                    {error.message}
                  </p>
                )}
              </div>
            )
          })}
        </div>
      </fieldset>

      <button
        type="submit"
        disabled={isSubmitting}
        className="rounded-lg bg-sky-600 px-4 py-2 font-semibold text-white transition hover:bg-sky-500 focus:outline-none focus:ring-2 focus:ring-sky-400 focus:ring-offset-2 focus:ring-offset-slate-950 disabled:cursor-not-allowed disabled:opacity-60"
      >
        {isSubmitting ? 'Guardando…' : 'Guardar perfil'}
      </button>
    </form>
  )
}
