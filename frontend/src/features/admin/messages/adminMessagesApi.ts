import { api } from '../../../services/api'

/** A message from the contact form, as the inbox shows it (HU006). */
export interface ContactMessage {
  id: number
  name: string
  email: string
  message: string
  receivedAt: string
  readAt: string | null
  read: boolean
}

/** The newest first. */
export async function listMessages(): Promise<ContactMessage[]> {
  const { data } = await api.get<ContactMessage[]>('/api/admin/messages')
  return data
}

export async function markMessageRead(id: number): Promise<ContactMessage> {
  const { data } = await api.put<ContactMessage>(
    `/api/admin/messages/${id}/read`,
  )
  return data
}

export async function deleteMessage(id: number): Promise<void> {
  await api.delete(`/api/admin/messages/${id}`)
}
