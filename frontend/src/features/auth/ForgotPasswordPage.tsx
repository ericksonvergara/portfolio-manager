import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link } from 'react-router-dom'
import { AUTH_BUTTON, AUTH_INPUT, AUTH_LINK, AuthCard } from './AuthCard'
import { requestPasswordReset } from './passwordResetApi'

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

/** Asks for a password recovery link by email (HU010). */
export function ForgotPasswordPage() {
  const [sentTo, setSentTo] = useState<string | null>(null)
  const [failed, setFailed] = useState(false)
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<{ email: string }>({ mode: 'onTouched' })

  const onSubmit = async ({ email }: { email: string }) => {
    setFailed(false)
    try {
      await requestPasswordReset(email.trim())
      setSentTo(email.trim())
    } catch {
      setFailed(true)
    }
  }

  return (
    <AuthCard
      titleId="forgot-title"
      title="Recuperar contraseña"
      subtitle="Te enviaremos un enlace para elegir una nueva."
    >
      {sentTo ? (
        <div role="status" className="space-y-4 text-sm text-slate-300">
          <p>
            Si <strong className="text-white">{sentTo}</strong> corresponde a
            una cuenta, recibirás un correo con un enlace para restablecer tu
            contraseña.
          </p>
          <p>
            El enlace vale 30 minutos y solo una vez. Revisa también la carpeta
            de spam.
          </p>
          <Link to="/login" className={AUTH_LINK}>
            Volver a iniciar sesión
          </Link>
        </div>
      ) : (
        <form
          noValidate
          onSubmit={handleSubmit(onSubmit)}
          className="space-y-5"
        >
          {failed && (
            <p
              role="alert"
              className="rounded-lg border border-red-800 bg-red-950 px-3 py-2 text-sm text-red-300"
            >
              No se pudo conectar con el servidor. Inténtalo de nuevo.
            </p>
          )}
          <div>
            <label
              htmlFor="email"
              className="mb-1 block text-sm font-medium text-slate-200"
            >
              Correo electrónico
            </label>
            <input
              id="email"
              type="email"
              autoComplete="email"
              aria-invalid={errors.email ? 'true' : 'false'}
              aria-describedby={errors.email ? 'email-error' : undefined}
              className={AUTH_INPUT}
              {...register('email', {
                required: 'El correo es obligatorio.',
                pattern: {
                  value: EMAIL_PATTERN,
                  message: 'Ingresa un correo válido.',
                },
              })}
            />
            {errors.email && (
              <p id="email-error" className="mt-1 text-sm text-red-400">
                {errors.email.message}
              </p>
            )}
          </div>
          <button type="submit" disabled={isSubmitting} className={AUTH_BUTTON}>
            {isSubmitting ? 'Enviando…' : 'Enviar enlace'}
          </button>
          <p className="text-center">
            <Link to="/login" className={AUTH_LINK}>
              Volver a iniciar sesión
            </Link>
          </p>
        </form>
      )}
    </AuthCard>
  )
}
