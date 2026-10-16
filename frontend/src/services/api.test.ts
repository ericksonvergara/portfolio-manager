import {
  AxiosError,
  type AxiosAdapter,
  type AxiosResponse,
  type InternalAxiosRequestConfig,
} from 'axios'
import { afterEach, describe, expect, it } from 'vitest'
import { api, setAccessToken, setSessionRenewer } from './api'

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

/** Answers 401 to the given Authorization headers and 200 to any other, recording what it saw. */
function rejecting(unauthorized: (authorization: unknown) => boolean) {
  const seen: unknown[] = []
  const adapter: AxiosAdapter = async (config) => {
    const authorization = config.headers.Authorization ?? null
    seen.push(authorization)
    if (unauthorized(authorization)) {
      throw new AxiosError(
        'Unauthorized',
        'ERR_BAD_REQUEST',
        config,
        undefined,
        { status: 401, config } as AxiosResponse,
      )
    }
    return { data: 'ok', status: 200, statusText: 'OK', headers: {}, config }
  }
  return { adapter, seen }
}

function renewTo(token: string | null) {
  const renewer = {
    calls: 0,
    renew: async () => {
      renewer.calls += 1
      setAccessToken(token)
      return token
    },
  }
  setSessionRenewer(renewer.renew)
  return renewer
}

describe('api', () => {
  afterEach(() => {
    setAccessToken(null)
    setSessionRenewer(null)
  })

  it('sends the access token as a Bearer header once it is set', async () => {
    setAccessToken('token-123')

    expect(await authorizationHeaderSent()).toBe('Bearer token-123')
  })

  it('sends no Authorization header without a token', async () => {
    expect(await authorizationHeaderSent()).toBeNull()
  })

  it('renews the session on a 401 and retries with the new token', async () => {
    setAccessToken('expired')
    renewTo('fresh')
    const { adapter, seen } = rejecting((auth) => auth === 'Bearer expired')

    const response = await api.get('/api/projects', { adapter })

    expect(response.data).toBe('ok')
    expect(seen).toEqual(['Bearer expired', 'Bearer fresh'])
  })

  it('gives up when the session cannot be renewed', async () => {
    setAccessToken('expired')
    renewTo(null)
    const { adapter, seen } = rejecting(() => true)

    await expect(api.get('/api/projects', { adapter })).rejects.toMatchObject({
      response: { status: 401 },
    })
    expect(seen).toHaveLength(1)
  })

  it('retries a request only once', async () => {
    setAccessToken('expired')
    const renewer = renewTo('fresh')
    const { adapter, seen } = rejecting(() => true)

    await expect(api.get('/api/projects', { adapter })).rejects.toMatchObject({
      response: { status: 401 },
    })
    expect(renewer.calls).toBe(1)
    expect(seen).toHaveLength(2)
  })

  it('does not renew the session for the auth endpoints', async () => {
    const renewer = renewTo('fresh')
    const { adapter } = rejecting(() => true)

    await expect(
      api.post('/api/auth/login', {}, { adapter }),
    ).rejects.toMatchObject({ response: { status: 401 } })
    expect(renewer.calls).toBe(0)
  })
})
