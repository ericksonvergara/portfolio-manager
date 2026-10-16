export interface AccessTokenClaims {
  sub: string
  email: string
  name: string
  roles: string[]
  iat: number
  exp: number
}

/**
 * Reads the claims of a JWT without verifying its signature. The backend verifies every request;
 * the frontend only uses the claims to decide what to show.
 */
export function decodeAccessToken(token: string): AccessTokenClaims {
  const payload = token.split('.')[1]
  if (!payload) {
    throw new Error('Malformed JWT')
  }

  const base64 = payload.replace(/-/g, '+').replace(/_/g, '/')
  const json = new TextDecoder().decode(
    Uint8Array.from(atob(base64), (char) => char.charCodeAt(0)),
  )
  const claims = JSON.parse(json) as Partial<AccessTokenClaims>

  return {
    sub: claims.sub ?? '',
    email: claims.email ?? '',
    name: claims.name ?? '',
    roles: Array.isArray(claims.roles) ? claims.roles : [],
    iat: claims.iat ?? 0,
    exp: claims.exp ?? 0,
  }
}
