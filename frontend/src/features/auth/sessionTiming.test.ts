import { describe, expect, it } from 'vitest'
import { RENEWAL_MARGIN_MS, renewalDelayMs } from './sessionTiming'

describe('renewalDelayMs', () => {
  it('renews one margin before a regular token expires', () => {
    // 15 minutes, the default access token lifetime.
    expect(renewalDelayMs({ iat: 1_000, exp: 1_900 })).toBe(
      900_000 - RENEWAL_MARGIN_MS,
    )
  })

  it('renews a short-lived token halfway through', () => {
    expect(renewalDelayMs({ iat: 1_000, exp: 1_010 })).toBe(5_000)
  })

  it('does not schedule a renewal when the token lifetime is unknown', () => {
    expect(renewalDelayMs({ iat: 0, exp: 1_900 })).toBeNull()
    expect(renewalDelayMs({ iat: 1_000, exp: 1_000 })).toBeNull()
  })
})
