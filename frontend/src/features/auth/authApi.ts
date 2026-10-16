import { api } from '../../services/api'

export interface LoginCredentials {
  email: string
  password: string
}

export interface TokenResponse {
  accessToken: string
  tokenType: string
  expiresAt: string
}

export async function login(
  credentials: LoginCredentials,
): Promise<TokenResponse> {
  const { data } = await api.post<TokenResponse>('/api/auth/login', credentials)
  return data
}
