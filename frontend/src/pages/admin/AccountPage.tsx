import { isAxiosError } from 'axios'
import { useForm } from 'react-hook-form'
import { useNavigate } from 'react-router-dom'
import { changePassword } from '../../features/admin/account/accountApi'
import {
  PASSWORD_POLICY_MESSAGE,
  PASSWORD_POLICY_PATTERN,
} from '../../features/auth/passwordPolicy'
import { useAuth } from '../../features/auth/useAuth'

interface PasswordValues {
  currentPassword: string
  newPassword: string
  confirmation: string
}

const INPUT =
  'w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-white outline-none focus:border-sky-500 focus:ring-2 focus:ring-sky-500/40 aria-[invalid=true]:border-red-500'

/** The backend answers 400 with these messages; each one belongs to a field of the form. */
const SERVER_ERRORS: Array<{
  match: string
  field: keyof PasswordValues
  message: string
}> = [
  {
    match: 'current password is incorrect',
    field: 'currentPassword',
    message: 'La contraseña actual no es correcta.',
  },
  {
    match: 'must be different',
    field: 'newPassword',
    message: 'La contraseña nueva debe ser distinta de la actual.',
  },
  {
    match: 'password must have',
    field: 'newPassword',
    message: PASSWORD_POLICY_MESSAGE,
  },
]

/** Changes the password of the signed-in administrator (HU011). */
export function AccountPage() {
  const { session, logout } = useAuth()
  const navigate = useNavigate()
  const {
    register,
    handleSubmit,
    getValues,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<PasswordValues>({ mode: 'onTouched' })

  const onSubmit = async ({ currentPassword, newPassword }: PasswordValues) => {
    try {
      await changePassword(currentPassword, newPassword)
    } catch (error) {
      const message = isAxiosError<{ message?: string }>(error)
        ? (error.response?.data?.message ?? '')
        : ''
      const known = SERVER_ERRORS.find((e) => message.includes(e.match))
      if (known) {
        setError(known.field, { message: known.message })
      } else {
        setError('root', {
          message: 'No se pudo cambiar la contraseña. Inténtalo de nuevo.',
        })
      }
      return
    }
    // The backend has closed every session: this browser signs in again with the new password.
    await logout()
    navigate('/login', { replace: true, state: { passwordChanged: true } })
  }

  const field = (
    name: keyof PasswordValues,
    label: string,
    autoComplete: string,
    rules: Parameters<typeof register>[1],
    help?: string,
  ) => (
    <div>
      <label
        htmlFor={name}
        className="mb-1 block text-sm font-medium text-slate-200"
      >
        {label}
      </label>
      <input
        id={name}
        type="password"
        autoComplete={autoComplete}
        aria-invalid={errors[name] ? 'true' : 'false'}
        aria-describedby={errors[name] || help ? `${name}-help` : undefined}
        className={INPUT}
        {...register(name, rules)}
      />
      {(errors[name] || help) && (
        <p
          id={`${name}-help`}
          className={`mt-1 text-sm ${errors[name] ? 'text-red-400' : 'text-slate-500'}`}
        >
          {errors[name]?.message ?? help}
        </p>
      )}
    </div>
  )

  return (
    <section aria-labelledby="account-title" className="max-w-md">
      <h1 id="account-title" className="text-2xl font-bold">
        Cuenta
      </h1>
      <p className="mt-1 text-sm text-slate-400">
        {session?.user.name} · {session?.user.email}
      </p>

      <h2 className="mt-8 text-lg font-semibold">Cambiar contraseña</h2>
      <p className="mt-1 text-sm text-slate-400">
        Al cambiarla se cerrarán todas tus sesiones, también esta.
      </p>

      <form
        noValidate
        onSubmit={handleSubmit(onSubmit)}
        className="mt-6 space-y-5"
      >
        {errors.root && (
          <p
            role="alert"
            className="rounded-lg border border-red-800 bg-red-950 px-3 py-2 text-sm text-red-300"
          >
            {errors.root.message}
          </p>
        )}
        {field('currentPassword', 'Contraseña actual', 'current-password', {
          required: 'La contraseña actual es obligatoria.',
        })}
        {field(
          'newPassword',
          'Contraseña nueva',
          'new-password',
          {
            required: 'La contraseña nueva es obligatoria.',
            pattern: {
              value: PASSWORD_POLICY_PATTERN,
              message: PASSWORD_POLICY_MESSAGE,
            },
            validate: (value) =>
              value !== getValues('currentPassword') ||
              'La contraseña nueva debe ser distinta de la actual.',
          },
          PASSWORD_POLICY_MESSAGE,
        )}
        {field('confirmation', 'Repite la contraseña nueva', 'new-password', {
          validate: (value) =>
            value === getValues('newPassword') ||
            'Las contraseñas no coinciden.',
        })}
        <button
          type="submit"
          disabled={isSubmitting}
          className="rounded-lg bg-sky-600 px-4 py-2 font-semibold text-white transition hover:bg-sky-500 focus:outline-none focus:ring-2 focus:ring-sky-400 focus:ring-offset-2 focus:ring-offset-slate-950 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {isSubmitting ? 'Guardando…' : 'Cambiar contraseña'}
        </button>
      </form>
    </section>
  )
}
