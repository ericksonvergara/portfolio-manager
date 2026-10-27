import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { AxiosError, type AxiosResponse } from 'axios'
import { MemoryRouter, Route, Routes, useLocation } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { AccountPage } from './AccountPage'

/* Plain functions instead of vi.fn(), for the same reason as in LoginPage.test.tsx. */
const fake = vi.hoisted(() => ({
  changes: [] as Array<{ current: string; next: string }>,
  answer: (): Promise<void> => Promise.resolve(),
  logouts: 0,
}))

vi.mock('../../features/admin/account/accountApi', () => ({
  changePassword: (current: string, next: string) => {
    fake.changes.push({ current, next })
    return fake.answer()
  },
}))

vi.mock('../../features/auth/useAuth', () => ({
  useAuth: () => ({
    session: { user: { name: 'Admin', email: 'admin@example.com' } },
    logout: async () => {
      fake.logouts++
    },
  }),
}))

function badRequest(message: string) {
  return new AxiosError(
    'Bad Request',
    'ERR_BAD_REQUEST',
    undefined,
    undefined,
    {
      status: 400,
      data: { message },
    } as AxiosResponse,
  )
}

function LoginStub() {
  const state = useLocation().state as { passwordChanged?: boolean } | null
  return (
    <p>Login{state?.passwordChanged ? ' tras cambiar la contraseña' : ''}</p>
  )
}

function renderPage() {
  render(
    <MemoryRouter initialEntries={['/admin/cuenta']}>
      <Routes>
        <Route path="/admin/cuenta" element={<AccountPage />} />
        <Route path="/login" element={<LoginStub />} />
      </Routes>
    </MemoryRouter>,
  )
}

async function fill(current: string, next: string, confirmation = next) {
  const user = userEvent.setup()
  await user.type(screen.getByLabelText('Contraseña actual'), current)
  await user.type(screen.getByLabelText('Contraseña nueva'), next)
  await user.type(
    screen.getByLabelText('Repite la contraseña nueva'),
    confirmation,
  )
  await user.click(screen.getByRole('button', { name: 'Cambiar contraseña' }))
}

beforeEach(() => {
  fake.changes = []
  fake.answer = () => Promise.resolve()
  fake.logouts = 0
})

describe('AccountPage', () => {
  it('changes the password, closes the session and opens the login', async () => {
    renderPage()
    expect(screen.getByText('Admin · admin@example.com')).toBeInTheDocument()

    await fill('Actual#123', 'Nueva#456')

    expect(
      await screen.findByText('Login tras cambiar la contraseña'),
    ).toBeInTheDocument()
    expect(fake.changes).toEqual([{ current: 'Actual#123', next: 'Nueva#456' }])
    expect(fake.logouts).toBe(1)
  })

  it('checks the policy, the confirmation and that the password changes before sending', async () => {
    renderPage()
    await fill('Actual#123', 'Actual#123', 'Otra#123')

    expect(
      await screen.findByText(
        'La contraseña nueva debe ser distinta de la actual.',
      ),
    ).toBeInTheDocument()
    expect(
      screen.getByText('Las contraseñas no coinciden.'),
    ).toBeInTheDocument()
    expect(fake.changes).toHaveLength(0)
  })

  it('marks the current password when it is wrong', async () => {
    fake.answer = () =>
      Promise.reject(badRequest('The current password is incorrect'))
    renderPage()
    await fill('Equivocada#1', 'Nueva#456')

    expect(
      await screen.findByText('La contraseña actual no es correcta.'),
    ).toBeInTheDocument()
    expect(screen.getByLabelText('Contraseña actual')).toHaveAttribute(
      'aria-invalid',
      'true',
    )
    expect(fake.logouts).toBe(0)
  })

  it('shows a general message when the server cannot be reached', async () => {
    fake.answer = () => Promise.reject(new Error('network'))
    renderPage()
    await fill('Actual#123', 'Nueva#456')

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'No se pudo cambiar la contraseña. Inténtalo de nuevo.',
    )
  })
})
