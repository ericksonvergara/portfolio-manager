import type { AccessTokenClaims } from './jwt'

/** How long before the access token expires the session is renewed. */
export const RENEWAL_MARGIN_MS = 60_000

// setTimeout fires immediately for longer delays.
const MAX_TIMEOUT_MS = 2_147_483_647

/**
 * Milliseconds from the moment the token is received until the session should be renewed, or null
 * when the token does not say how long it lives. It uses the token's own lifetime (exp - iat, both
 * from the server clock), so a wrong clock on the user's machine does not matter. Short-lived
 * tokens are renewed halfway through so the renewal never spins in a tight loop.
 */
export function renewalDelayMs(
  claims: Pick<AccessTokenClaims, 'iat' | 'exp'>,
): number | null {
  const lifetimeMs = (claims.exp - claims.iat) * 1000
  if (claims.iat <= 0 || !(lifetimeMs > 0)) {
    return null
  }
  return Math.min(
    Math.max(lifetimeMs / 2, lifetimeMs - RENEWAL_MARGIN_MS),
    MAX_TIMEOUT_MS,
  )
}
