function base64Url(value: string): string {
  const bytes = new TextEncoder().encode(value)
  const binary = Array.from(bytes, (byte) => String.fromCharCode(byte)).join('')
  return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '')
}

/** Builds an unsigned JWT with the given claims, enough for code that only reads them. */
export function fakeJwt(claims: Record<string, unknown>): string {
  return `${base64Url(JSON.stringify({ alg: 'HS256' }))}.${base64Url(JSON.stringify(claims))}.signature`
}
