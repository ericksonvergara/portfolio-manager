import type { AxiosAdapter } from 'axios'
import { afterEach, describe, expect, it } from 'vitest'
import { api } from '../../services/api'
import { logout, refresh } from './authApi'

const originalAdapter = api.defaults.adapter

function countRequests(): { count: number } {
  const requests = { count: 0 }
  const adapter: AxiosAdapter = async (config) => {
    requests.count += 1
    return {
      data: {
        accessToken: `token-${requests.count}`,
        tokenType: 'Bearer',
        expiresAt: '2099-01-01T00:00:00Z',
      },
      status: 200,
      statusText: 'OK',
      headers: {},
      config,
    }
  }
  api.defaults.adapter = adapter
  return requests
}

describe('refresh', () => {
  afterEach(() => {
    api.defaults.adapter = originalAdapter
  })

  it('shares one request between concurrent callers', async () => {
    const requests = countRequests()

    const [first, second] = await Promise.all([refresh(), refresh()])

    expect(requests.count).toBe(1)
    expect(second).toBe(first)
  })

  it('sends a new request once the previous one is over', async () => {
    const requests = countRequests()

    await refresh()
    const later = await refresh()

    expect(requests.count).toBe(2)
    expect(later.accessToken).toBe('token-2')
  })
})

describe('logout', () => {
  afterEach(() => {
    api.defaults.adapter = originalAdapter
  })

  it('waits for a refresh in progress before closing the session', async () => {
    const sent: string[] = []
    let finishRefresh = () => {}
    api.defaults.adapter = async (config) => {
      sent.push(config.url ?? '')
      if (config.url === '/api/auth/refresh') {
        await new Promise<void>((resolve) => (finishRefresh = resolve))
      }
      return { data: {}, status: 200, statusText: 'OK', headers: {}, config }
    }

    const refreshing = refresh()
    const loggingOut = logout()
    await new Promise((resolve) => setTimeout(resolve, 20))
    expect(sent).toEqual(['/api/auth/refresh'])

    finishRefresh()
    await Promise.all([refreshing, loggingOut])
    expect(sent).toEqual(['/api/auth/refresh', '/api/auth/logout'])
  })
})
