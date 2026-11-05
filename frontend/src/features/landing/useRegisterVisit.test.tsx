import { renderHook, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { StrictMode } from 'react'
import { useRegisterVisit } from './useRegisterVisit'

/* Plain functions instead of vi.fn(), for the same reason as in LoginPage.test.tsx. */
const api = vi.hoisted(() => ({
  visits: [] as Array<string | undefined>,
  answer: (): Promise<void> => Promise.resolve(),
}))

vi.mock('./visitsApi', () => ({
  registerVisit: (slug?: string) => {
    api.visits.push(slug)
    return api.answer()
  },
}))

beforeEach(() => {
  api.visits = []
  api.answer = () => Promise.resolve()
})

describe('useRegisterVisit (HU025)', () => {
  it('registers a visit to the landing page once, even in strict mode', async () => {
    renderHook(() => useRegisterVisit(), { wrapper: StrictMode })

    await waitFor(() => expect(api.visits).toEqual([undefined]))
  })

  it('waits until the page is ready and sends its project', async () => {
    const { rerender } = renderHook(
      ({ ready }) => useRegisterVisit('sistema-erp', ready),
      { initialProps: { ready: false } },
    )
    expect(api.visits).toEqual([])

    rerender({ ready: true })
    await waitFor(() => expect(api.visits).toEqual(['sistema-erp']))
  })

  it('registers a new visit when the project changes', async () => {
    const { rerender } = renderHook(({ slug }) => useRegisterVisit(slug), {
      initialProps: { slug: 'sistema-erp' },
    })
    rerender({ slug: 'tienda' })

    await waitFor(() => expect(api.visits).toEqual(['sistema-erp', 'tienda']))
  })

  it('ignores a failure', async () => {
    api.answer = () => Promise.reject(new Error('network'))

    renderHook(() => useRegisterVisit())

    await waitFor(() => expect(api.visits).toHaveLength(1))
  })
})
