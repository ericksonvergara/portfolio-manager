import { isAxiosError } from 'axios'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import type { LoginCredentials } from './authApi'
import { NotAdminError } from './authContext'
import {
  PASSWORD_POLICY_MESSAGE,
  PASSWORD_POLICY_PATTERN,
} from './passwordPolicy'
import { SessionRestoring } from './SessionRestoring'
import { useAuth } from './useAuth'

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const DEFAULT_REDIRECT = '/admin'

function toErrorMessage(error: unknown): string {
  if (error instanceof NotAdminError) {
    return 'Tu cuenta no tiene permisos de administrador.'
  }
  if (isAxiosError(error) && error.response?.status === 401) {
    return 'Correo o contraseña incorrectos.'
  }
  if (isAxiosError(error) && error.response?.status === 400) {
    return 'Revisa los datos ingresados.'
  }
  return 'No se pudo conectar con el servidor. Inténtalo de nuevo.'
}

export function LoginPage() {
  const { login, isAuthenticated, isAdmin, isRestoring, sessionExpired } =
    useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [submitError, setSubmitError] = useState<string | null>(null)
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginCredentials>({ mode: 'onTouched' })

  const state = location.state as {
    from?: string
    passwordReset?: boolean
    passwordChanged?: boolean
  } | null
  const passwordNotice = state?.passwordReset || state?.passwordChanged
  const redirectTo = state?.from ?? DEFAULT_REDIRECT

  if (isRestoring) {
    return <SessionRestoring />
  }

  if (isAuthenticated && isAdmin) {
    return <Navigate to={redirectTo} replace />
  }

  const onSubmit = async (credentials: LoginCredentials) => {
    setSubmitError(null)
    try {
      await login({
        email: credentials.email.trim(),
        password: credentials.password,
      })
      navigate(redirectTo, { replace: true })
    } catch (error) {
      setSubmitError(toErrorMessage(error))
    }
  }

  return (
    <main className="flex min-h-screen items-center justify-center bg-slate-950 px-4">
      <section
        aria-labelledby="login-title"
        className="w-full max-w-sm rounded-2xl border border-slate-800 bg-slate-900 p-8 shadow-xl"
      >
        <h1 id="login-title" className="mb-1 text-2xl font-bold text-white">
          Iniciar sesión
        </h1>
        <p className="mb-6 text-sm text-slate-400">
          Panel administrativo del portafolio
        </p>

        {submitError && (
          <p
            role="alert"
            className="mb-4 rounded-lg border border-red-800 bg-red-950 px-3 py-2 text-sm text-red-300"
          >
            {submitError}
          </p>
        )}

        {passwordNotice && !submitError && (
          <p
            role="status"
            className="mb-4 rounded-lg border border-emerald-800 bg-emerald-950 px-3 py-2 text-sm text-emerald-200"
          >
            {state?.passwordChanged
              ? 'Contraseña cambiada. Por seguridad se cerraron todas tus sesiones: inicia sesión con la nueva.'
              : 'Contraseña actualizada. Inicia sesión con la nueva.'}
          </p>
        )}

        {sessionExpired && !submitError && !passwordNotice && (
          <p
            role="status"
            className="mb-4 rounded-lg border border-amber-800 bg-amber-950 px-3 py-2 text-sm text-amber-200"
          >
            Tu sesión expiró. Vuelve a iniciar sesión.
          </p>
        )}

        <form
          noValidate
          onSubmit={handleSubmit(onSubmit)}
          className="space-y-5"
        >
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
              className="w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-white outline-none focus:border-sky-500 focus:ring-2 focus:ring-sky-500/40 aria-[invalid=true]:border-red-500"
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

          <div>
            <label
              htmlFor="password"
              className="mb-1 block text-sm font-medium text-slate-200"
            >
              Contraseña
            </label>
            <input
              id="password"
              type="password"
              autoComplete="current-password"
              aria-invalid={errors.password ? 'true' : 'false'}
              aria-describedby={errors.password ? 'password-error' : undefined}
              className="w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-white outline-none focus:border-sky-500 focus:ring-2 focus:ring-sky-500/40 aria-[invalid=true]:border-red-500"
              {...register('password', {
                required: 'La contraseña es obligatoria.',
                pattern: {
                  value: PASSWORD_POLICY_PATTERN,
                  message: PASSWORD_POLICY_MESSAGE,
                },
              })}
            />
            {errors.password && (
              <p id="password-error" className="mt-1 text-sm text-red-400">
                {errors.password.message}
              </p>
            )}
          </div>

          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full rounded-lg bg-sky-600 px-4 py-2 font-semibold text-white transition hover:bg-sky-500 focus:outline-none focus:ring-2 focus:ring-sky-400 focus:ring-offset-2 focus:ring-offset-slate-900 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {isSubmitting ? 'Ingresando…' : 'Ingresar'}
          </button>

          <p className="text-center">
            <Link
              to="/recuperar-contrasena"
              className="text-sm text-sky-400 underline-offset-4 hover:underline focus:outline-none focus:ring-2 focus:ring-sky-400"
            >
              ¿Olvidaste tu contraseña?
            </Link>
          </p>
        </form>
      </section>
    </main>
  )
}
