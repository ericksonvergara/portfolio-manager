import { describe, expect, it } from 'vitest'
import { fakeJwt } from '../../test/fakeJwt'
import { decodeAccessToken } from './jwt'

describe('decodeAccessToken', () => {
  it('reads the claims, including non-ASCII characters', () => {
    const token = fakeJwt({
      sub: '1',
      email: 'admin@example.com',
      name: 'José Muñoz',
      roles: ['ADMIN'],
      iat: 1,
      exp: 2,
    })

    expect(decodeAccessToken(token)).toEqual({
      sub: '1',
      email: 'admin@example.com',
      name: 'José Muñoz',
      roles: ['ADMIN'],
      iat: 1,
      exp: 2,
    })
  })

  it('defaults missing roles to an empty list', () => {
    expect(decodeAccessToken(fakeJwt({ sub: '1' })).roles).toEqual([])
  })

  it('rejects a malformed token', () => {
    expect(() => decodeAccessToken('not-a-jwt')).toThrow('Malformed JWT')
  })
})
