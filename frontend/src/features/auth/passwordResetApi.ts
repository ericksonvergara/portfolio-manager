import { api } from '../../services/api'

/** Asks for a recovery link (HU010). The answer is the same whether the email exists or not. */
export async function requestPasswordReset(email: string): Promise<void> {
  await api.post('/api/auth/password-reset', { email })
}

/** Sets the new password with the token of the link; every open session is closed. */
export async function confirmPasswordReset(
  token: string,
  newPassword: string,
): Promise<void> {
  await api.post('/api/auth/password-reset/confirm', { token, newPassword })
}
