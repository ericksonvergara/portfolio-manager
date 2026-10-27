import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ForgotPasswordPage } from './ForgotPasswordPage'

/* Plain functions instead of vi.fn(), for the same reason as in LoginPage.test.tsx. */
const api = vi.hoisted(() => ({
  requested: [] as string[],
  answer: (): Promise<void> => Promise.resolve(),
}))

vi.mock('./passwordResetApi', () => ({
  requestPasswordReset: (email: string) => {
    api.requested.push(email)
    return api.answer()
  },
}))

function renderPage() {
  render(
    <MemoryRouter>
      <ForgotPasswordPage />
    </MemoryRouter>,
  )
}

beforeEach(() => {
  api.requested = []
  api.answer = () => Promise.resolve()
})

describe('ForgotPasswordPage', () => {
  it('asks for a link and gives the same answer whether the account exists or not', async () => {
    const user = userEvent.setup()
    renderPage()
    await user.type(
      screen.getByLabelText('Correo electrónico'),
      ' admin@example.com ',
    )
    await user.click(screen.getByRole('button', { name: 'Enviar enlace' }))

    expect(await screen.findByRole('status')).toHaveTextContent(
      'Si admin@example.com corresponde a una cuenta, recibirás un correo',
    )
    expect(
      screen.getByText(/vale 30 minutos y solo una vez/),
    ).toBeInTheDocument()
    expect(api.requested).toEqual(['admin@example.com'])
  })

  it('validates the email before sending it', async () => {
    const user = userEvent.setup()
    renderPage()
    await user.type(screen.getByLabelText('Correo electrónico'), 'no-es-correo')
    await user.click(screen.getByRole('button', { name: 'Enviar enlace' }))

    expect(
      await screen.findByText('Ingresa un correo válido.'),
    ).toBeInTheDocument()
    expect(api.requested).toHaveLength(0)
  })

  it('explains when the server cannot be reached', async () => {
    api.answer = () => Promise.reject(new Error('network'))
    const user = userEvent.setup()
    renderPage()
    await user.type(
      screen.getByLabelText('Correo electrónico'),
      'admin@example.com',
    )
    await user.click(screen.getByRole('button', { name: 'Enviar enlace' }))

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'No se pudo conectar con el servidor.',
    )
  })
})
