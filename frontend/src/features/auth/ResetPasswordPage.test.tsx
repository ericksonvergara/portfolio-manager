import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { AxiosError, type AxiosResponse } from 'axios'
import { MemoryRouter, Route, Routes, useLocation } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ResetPasswordPage } from './ResetPasswordPage'

/* Plain functions instead of vi.fn(), for the same reason as in LoginPage.test.tsx. */
const api = vi.hoisted(() => ({
  confirmed: [] as Array<{ token: string; password: string }>,
  answer: (): Promise<void> => Promise.resolve(),
}))

vi.mock('./passwordResetApi', () => ({
  confirmPasswordReset: (token: string, password: string) => {
    api.confirmed.push({ token, password })
    return api.answer()
  },
}))

function LoginStub() {
  const state = useLocation().state as { passwordReset?: boolean } | null
  return <p>Login{state?.passwordReset ? ' tras restablecer' : ''}</p>
}

function renderPage(search = '?token=abc%2B1') {
  render(
    <MemoryRouter initialEntries={[`/restablecer-contrasena${search}`]}>
      <Routes>
        <Route path="/restablecer-contrasena" element={<ResetPasswordPage />} />
        <Route path="/login" element={<LoginStub />} />
      </Routes>
    </MemoryRouter>,
  )
}

async function fill(password: string, confirmation = password) {
  const user = userEvent.setup()
  await user.type(screen.getByLabelText('Contraseña nueva'), password)
  await user.type(screen.getByLabelText('Repite la contraseña'), confirmation)
  await user.click(screen.getByRole('button', { name: 'Guardar contraseña' }))
}

beforeEach(() => {
  api.confirmed = []
  api.answer = () => Promise.resolve()
})

describe('ResetPasswordPage', () => {
  it('sets the new password with the token of the link and opens the login', async () => {
    renderPage()
    await fill('Nueva#123')

    expect(
      await screen.findByText('Login tras restablecer'),
    ).toBeInTheDocument()
    expect(api.confirmed).toEqual([{ token: 'abc+1', password: 'Nueva#123' }])
  })

  it('checks the password policy and the confirmation before sending', async () => {
    renderPage()
    await fill('corta', 'otra')

    expect(
      await screen.findByText(
        'Debe tener entre 6 y 12 caracteres, con mayúscula, minúscula, número y carácter especial.',
      ),
    ).toBeInTheDocument()
    expect(
      screen.getByText('Las contraseñas no coinciden.'),
    ).toBeInTheDocument()
    expect(screen.getByLabelText('Contraseña nueva')).toHaveAttribute(
      'aria-invalid',
      'true',
    )
    expect(api.confirmed).toHaveLength(0)
  })

  it('offers a new link when this one is invalid, used or expired', async () => {
    api.answer = () =>
      Promise.reject(
        new AxiosError('Bad Request', 'ERR_BAD_REQUEST', undefined, undefined, {
          status: 400,
        } as AxiosResponse),
      )
    renderPage()
    await fill('Nueva#123')

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'El enlace no es válido o ha caducado.',
    )
    expect(
      screen.getByRole('link', { name: 'Solicitar un enlace nuevo' }),
    ).toHaveAttribute('href', '/recuperar-contrasena')
  })

  it('says so directly when the link has no token', () => {
    renderPage('')

    expect(screen.getByRole('alert')).toHaveTextContent(
      'El enlace no es válido',
    )
    expect(screen.queryByLabelText('Contraseña nueva')).not.toBeInTheDocument()
  })
})
