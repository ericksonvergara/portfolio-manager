import type { InternalAxiosRequestConfig } from 'axios'
import { afterEach, describe, expect, it } from 'vitest'
import { api, setAccessToken } from './api'

async function authorizationHeaderSent(): Promise<unknown> {
  const response = await api.get('/api/anything', {
    adapter: async (config: InternalAxiosRequestConfig) => ({
      data: config.headers.Authorization ?? null,
      status: 200,
      statusText: 'OK',
      headers: {},
      config,
    }),
  })
  return response.data
}

describe('api', () => {
  afterEach(() => setAccessToken(null))

  it('sends the access token as a Bearer header once it is set', async () => {
    setAccessToken('token-123')

    expect(await authorizationHeaderSent()).toBe('Bearer token-123')
  })

  it('sends no Authorization header without a token', async () => {
    expect(await authorizationHeaderSent()).toBeNull()
  })
})
