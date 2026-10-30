import { isAxiosError } from 'axios'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { EMAIL_PATTERN } from '../../services/validation'
import { sendContactMessage, type ContactInput } from './landingApi'

const INPUT_CLASS =
  'w-full rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-white outline-none placeholder:text-slate-500 focus:border-sky-500 focus:ring-2 focus:ring-sky-500/40 aria-[invalid=true]:border-red-500'

type Field = 'name' | 'email' | 'message'

const EMPTY: ContactInput = { name: '', email: '', message: '', website: '' }

function failureMessage(error: unknown): string {
  const status = isAxiosError(error) ? error.response?.status : undefined
  if (status === 429) {
    return 'Has enviado varios mensajes seguidos. Inténtalo de nuevo más tarde.'
  }
  if (status === 400) {
    return 'Revisa los datos del formulario.'
  }
  return 'No se pudo enviar el mensaje. Inténtalo de nuevo o escríbeme por correo.'
}

/**
 * Contact form of the landing page (HU006): it validates the data before sending, and the backend
 * saves the message for the administrator and notifies the owner by email.
 */
export function ContactForm() {
  const [sent, setSent] = useState(false)
  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<ContactInput>({ mode: 'onTouched', defaultValues: EMPTY })

  const submit = async (values: ContactInput) => {
    try {
      await sendContactMessage({
        name: values.name.trim(),
        email: values.email.trim(),
        message: values.message.trim(),
        website: values.website,
      })
      reset(EMPTY)
      setSent(true)
    } catch (error) {
      setError('root', { message: failureMessage(error) })
    }
  }

  if (sent) {
    return (
      <div
        role="status"
        className="rounded-2xl border border-emerald-800 bg-emerald-950/60 p-5 text-emerald-200"
      >
        <p className="font-semibold">¡Gracias! Tu mensaje fue enviado.</p>
        <p className="mt-1 text-sm">Te responderé lo antes posible.</p>
        <button
          type="button"
          onClick={() => setSent(false)}
          className="mt-4 rounded-lg border border-emerald-700 px-3 py-1.5 text-sm font-semibold hover:bg-emerald-900 focus:outline-none focus-visible:ring-2 focus-visible:ring-emerald-400"
        >
          Enviar otro mensaje
        </button>
      </div>
    )
  }

  const a11y = (name: Field) => ({
    'aria-invalid': errors[name] ? ('true' as const) : ('false' as const),
    'aria-describedby': errors[name] ? `contact-${name}-error` : undefined,
  })

  const fieldError = (name: Field) =>
    errors[name] ? (
      <p id={`contact-${name}-error`} className="mt-1 text-sm text-red-400">
        {errors[name]?.message}
      </p>
    ) : null

  const required = (label: string, max: number) => ({
    validate: (value: string) =>
      value.trim() !== '' || `${label} es obligatorio.`,
    maxLength: { value: max, message: `Máximo ${max} caracteres.` },
  })

  return (
    <form
      noValidate
      onSubmit={handleSubmit(submit)}
      aria-label="Formulario de contacto"
      className="space-y-4"
    >
      {errors.root && (
        <p
          role="alert"
          className="rounded-lg border border-red-800 bg-red-950 px-3 py-2 text-sm text-red-300"
        >
          {errors.root.message}
        </p>
      )}
      <div className="grid gap-4 sm:grid-cols-2">
        <div>
          <label
            htmlFor="contact-name"
            className="mb-1 block text-sm font-medium text-slate-300"
          >
            Nombre
          </label>
          <input
            id="contact-name"
            autoComplete="name"
            className={INPUT_CLASS}
            {...a11y('name')}
            {...register('name', required('El nombre', 100))}
          />
          {fieldError('name')}
        </div>
        <div>
          <label
            htmlFor="contact-email"
            className="mb-1 block text-sm font-medium text-slate-300"
          >
            Correo
          </label>
          <input
            id="contact-email"
            type="email"
            autoComplete="email"
            className={INPUT_CLASS}
            {...a11y('email')}
            {...register('email', {
              ...required('El correo', 150),
              pattern: {
                value: EMAIL_PATTERN,
                message:
                  'Ingresa un correo válido, por ejemplo nombre@dominio.com.',
              },
            })}
          />
          {fieldError('email')}
        </div>
      </div>
      <div>
        <label
          htmlFor="contact-message"
          className="mb-1 block text-sm font-medium text-slate-300"
        >
          Mensaje
        </label>
        <textarea
          id="contact-message"
          rows={5}
          className={INPUT_CLASS}
          {...a11y('message')}
          {...register('message', required('El mensaje', 2000))}
        />
        {fieldError('message')}
      </div>
      {/* Anti-spam: hidden from people and screen readers; only bots fill it in. */}
      <div
        aria-hidden="true"
        className="absolute -left-[9999px] h-px w-px overflow-hidden"
      >
        <label htmlFor="contact-website">No rellenes este campo</label>
        <input
          id="contact-website"
          tabIndex={-1}
          autoComplete="off"
          {...register('website')}
        />
      </div>
      <button
        type="submit"
        disabled={isSubmitting}
        className="rounded-lg bg-sky-600 px-4 py-2 font-semibold text-white transition hover:bg-sky-500 focus:outline-none focus-visible:ring-2 focus-visible:ring-sky-300 focus-visible:ring-offset-2 focus-visible:ring-offset-slate-950 disabled:cursor-not-allowed disabled:opacity-60"
      >
        {isSubmitting ? 'Enviando…' : 'Enviar mensaje'}
      </button>
    </form>
  )
}
