import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import type { ContactMessage } from '../../features/admin/messages/adminMessagesApi'
import { MessagesPage } from './MessagesPage'

/* Plain functions instead of vi.fn(), for the same reason as in LoginPage.test.tsx. */
const api = vi.hoisted(() => ({
  list: (): Promise<ContactMessage[]> => Promise.resolve([]),
  calls: [] as string[],
  read: (): Promise<ContactMessage> => Promise.reject(new Error('not set')),
  remove: (): Promise<void> => Promise.resolve(),
}))

vi.mock('../../features/admin/messages/adminMessagesApi', () => ({
  listMessages: () => api.list(),
  markMessageRead: (id: number) => {
    api.calls.push(`read ${id}`)
    return api.read()
  },
  deleteMessage: (id: number) => {
    api.calls.push(`delete ${id}`)
    return api.remove()
  },
}))

const NEW: ContactMessage = {
  id: 2,
  name: 'Luis',
  email: 'luis@example.com',
  message: 'Me interesa tu proyecto.',
  receivedAt: '2026-10-29T15:30:00Z',
  readAt: null,
  read: false,
}

const OLD: ContactMessage = {
  id: 1,
  name: 'Ana Pérez',
  email: 'ana@example.com',
  message: 'Hola, ¿hablamos?',
  receivedAt: '2026-10-28T09:00:00Z',
  readAt: '2026-10-28T10:00:00Z',
  read: true,
}

beforeEach(() => {
  api.list = () => Promise.resolve([NEW, OLD])
  api.calls = []
  api.read = () =>
    Promise.resolve({ ...NEW, read: true, readAt: '2026-10-29T16:00:00Z' })
  api.remove = () => Promise.resolve()
})

describe('MessagesPage (HU006)', () => {
  it('lists the messages, newest first, and counts the unread ones', async () => {
    render(<MessagesPage />)

    expect(
      await screen.findByText('2 mensajes · 1 sin leer'),
    ).toBeInTheDocument()
    const cards = screen.getAllByRole('article')
    expect(cards.map((c) => c.getAttribute('aria-label'))).toEqual([
      'Mensaje de Luis',
      'Mensaje de Ana Pérez',
    ])
    expect(within(cards[0]).getByText('Sin leer')).toBeInTheDocument()
    expect(
      within(cards[0]).getByText('Me interesa tu proyecto.'),
    ).toBeInTheDocument()
    expect(
      within(cards[0]).getByRole('link', { name: 'Responder' }),
    ).toHaveAttribute(
      'href',
      expect.stringContaining('mailto:luis@example.com'),
    )
    expect(within(cards[1]).queryByText('Sin leer')).not.toBeInTheDocument()
  })

  it('marks a message as read', async () => {
    const user = userEvent.setup()
    render(<MessagesPage />)
    const card = await screen.findByRole('article', { name: 'Mensaje de Luis' })
    await user.click(
      within(card).getByRole('button', { name: 'Marcar como leído' }),
    )

    expect(await screen.findByText('2 mensajes')).toBeInTheDocument()
    expect(within(card).queryByText('Sin leer')).not.toBeInTheDocument()
    expect(api.calls).toEqual(['read 2'])
  })

  it('deletes a message after confirming', async () => {
    const user = userEvent.setup()
    render(<MessagesPage />)
    const card = await screen.findByRole('article', {
      name: 'Mensaje de Ana Pérez',
    })
    await user.click(within(card).getByRole('button', { name: 'Eliminar' }))
    await user.click(
      within(screen.getByRole('alertdialog')).getByRole('button', {
        name: 'Eliminar',
      }),
    )

    expect(
      await screen.findByText('Mensaje de Ana Pérez eliminado.'),
    ).toBeInTheDocument()
    expect(screen.getAllByRole('article')).toHaveLength(1)
    expect(api.calls).toEqual(['delete 1'])
  })

  it('shows an empty inbox', async () => {
    api.list = () => Promise.resolve([])
    render(<MessagesPage />)

    expect(
      await screen.findByText('Aún no has recibido mensajes.'),
    ).toBeInTheDocument()
  })
})
