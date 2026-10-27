import { api } from '../../../services/api'

/** Changes the password of the signed-in administrator; every session is closed (HU011). */
export async function changePassword(
  currentPassword: string,
  newPassword: string,
): Promise<void> {
  await api.put('/api/admin/account/password', { currentPassword, newPassword })
}
