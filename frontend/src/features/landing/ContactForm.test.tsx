import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { AxiosError, type AxiosResponse } from 'axios'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ContactForm } from './ContactForm'
import type { ContactInput } from './landingApi'

/* Plain functions instead of vi.fn(), for the same reason as in LoginPage.test.tsx. */
const api = vi.hoisted(() => ({
  sent: [] as ContactInput[],
  answer: (): Promise<void> => Promise.resolve(),
}))

vi.mock('./landingApi', () => ({
  sendContactMessage: (input: ContactInput) => {
    api.sent.push(input)
    return api.answer()
  },
}))

function httpError(status: number) {
  return new AxiosError(
    'Request failed',
    'ERR_BAD_REQUEST',
    undefined,
    undefined,
    {
      status,
    } as AxiosResponse,
  )
}

async function fill(user: ReturnType<typeof userEvent.setup>) {
  await user.type(screen.getByLabelText('Nombre'), '  Ana Pérez ')
  await user.type(screen.getByLabelText('Correo'), 'ana@example.com')
  await user.type(screen.getByLabelText('Mensaje'), 'Hola, ¿hablamos?')
}

beforeEach(() => {
  api.sent = []
  api.answer = () => Promise.resolve()
})

describe('ContactForm (HU006)', () => {
  it('sends the name, the email and the message, and thanks the visitor', async () => {
    const user = userEvent.setup()
    render(<ContactForm />)
    await fill(user)
    await user.click(screen.getByRole('button', { name: 'Enviar mensaje' }))

    expect(
      await screen.findByText('¡Gracias! Tu mensaje fue enviado.'),
    ).toBeInTheDocument()
    expect(api.sent).toEqual([
      {
        name: 'Ana Pérez',
        email: 'ana@example.com',
        message: 'Hola, ¿hablamos?',
        website: '',
      },
    ])

    await user.click(
      screen.getByRole('button', { name: 'Enviar otro mensaje' }),
    )
    expect(screen.getByLabelText('Nombre')).toHaveValue('')
  })

  it('validates the data before sending', async () => {
    const user = userEvent.setup()
    render(<ContactForm />)
    await user.type(screen.getByLabelText('Correo'), 'ana@example')
    await user.click(screen.getByRole('button', { name: 'Enviar mensaje' }))

    expect(
      await screen.findByText('El nombre es obligatorio.'),
    ).toBeInTheDocument()
    expect(screen.getByText(/Ingresa un correo válido/)).toBeInTheDocument()
    expect(screen.getByText('El mensaje es obligatorio.')).toBeInTheDocument()
    expect(screen.getByLabelText('Correo')).toHaveAttribute(
      'aria-invalid',
      'true',
    )
    expect(api.sent).toEqual([])
  })

  it('keeps the anti-spam field out of reach of people', () => {
    render(<ContactForm />)
    const trap = screen.getByLabelText('No rellenes este campo')

    expect(trap).toHaveAttribute('tabindex', '-1')
    expect(trap.closest('[aria-hidden="true"]')).not.toBeNull()
  })

  it('explains when the visitor sent too many messages', async () => {
    api.answer = () => Promise.reject(httpError(429))
    const user = userEvent.setup()
    render(<ContactForm />)
    await fill(user)
    await user.click(screen.getByRole('button', { name: 'Enviar mensaje' }))

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Has enviado varios mensajes seguidos',
    )
    // The text stays, so the visitor does not lose it.
    expect(screen.getByLabelText('Mensaje')).toHaveValue('Hola, ¿hablamos?')
  })

  it('offers the email when the message cannot be sent', async () => {
    api.answer = () => Promise.reject(new Error('network'))
    const user = userEvent.setup()
    render(<ContactForm />)
    await fill(user)
    await user.click(screen.getByRole('button', { name: 'Enviar mensaje' }))

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'escríbeme por correo',
    )
  })
})
