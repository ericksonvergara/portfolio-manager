import { isAxiosError } from 'axios'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { AUTH_BUTTON, AUTH_INPUT, AUTH_LINK, AuthCard } from './AuthCard'
import { confirmPasswordReset } from './passwordResetApi'
import {
  PASSWORD_POLICY_MESSAGE,
  PASSWORD_POLICY_PATTERN,
} from './passwordPolicy'

interface ResetValues {
  password: string
  confirmation: string
}

/** Sets a new password from the link of the recovery email (HU010). */
export function ResetPasswordPage() {
  const token = useSearchParams()[0].get('token')
  const navigate = useNavigate()
  const [invalidLink, setInvalidLink] = useState(!token)
  const [failed, setFailed] = useState(false)
  const {
    register,
    handleSubmit,
    getValues,
    formState: { errors, isSubmitting },
  } = useForm<ResetValues>({ mode: 'onTouched' })

  const onSubmit = async ({ password }: ResetValues) => {
    if (!token) return
    setFailed(false)
    try {
      await confirmPasswordReset(token, password)
      navigate('/login', { replace: true, state: { passwordReset: true } })
    } catch (error) {
      if (isAxiosError(error) && error.response?.status === 400) {
        setInvalidLink(true)
      } else {
        setFailed(true)
      }
    }
  }

  return (
    <AuthCard
      titleId="reset-title"
      title="Nueva contraseña"
      subtitle="Elige la contraseña con la que vas a iniciar sesión."
    >
      {invalidLink ? (
        <div role="alert" className="space-y-4 text-sm text-slate-300">
          <p>
            El enlace no es válido o ha caducado. Los enlaces valen 30 minutos y
            solo una vez.
          </p>
          <Link to="/recuperar-contrasena" className={AUTH_LINK}>
            Solicitar un enlace nuevo
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
              htmlFor="password"
              className="mb-1 block text-sm font-medium text-slate-200"
            >
              Contraseña nueva
            </label>
            <input
              id="password"
              type="password"
              autoComplete="new-password"
              aria-invalid={errors.password ? 'true' : 'false'}
              aria-describedby="password-help"
              className={AUTH_INPUT}
              {...register('password', {
                required: 'La contraseña es obligatoria.',
                pattern: {
                  value: PASSWORD_POLICY_PATTERN,
                  message: PASSWORD_POLICY_MESSAGE,
                },
              })}
            />
            <p
              id="password-help"
              className={`mt-1 text-sm ${errors.password ? 'text-red-400' : 'text-slate-500'}`}
            >
              {errors.password?.message ?? PASSWORD_POLICY_MESSAGE}
            </p>
          </div>
          <div>
            <label
              htmlFor="confirmation"
              className="mb-1 block text-sm font-medium text-slate-200"
            >
              Repite la contraseña
            </label>
            <input
              id="confirmation"
              type="password"
              autoComplete="new-password"
              aria-invalid={errors.confirmation ? 'true' : 'false'}
              aria-describedby={
                errors.confirmation ? 'confirmation-error' : undefined
              }
              className={AUTH_INPUT}
              {...register('confirmation', {
                validate: (value) =>
                  value === getValues('password') ||
                  'Las contraseñas no coinciden.',
              })}
            />
            {errors.confirmation && (
              <p id="confirmation-error" className="mt-1 text-sm text-red-400">
                {errors.confirmation.message}
              </p>
            )}
          </div>
          <button type="submit" disabled={isSubmitting} className={AUTH_BUTTON}>
            {isSubmitting ? 'Guardando…' : 'Guardar contraseña'}
          </button>
        </form>
      )}
    </AuthCard>
  )
}
