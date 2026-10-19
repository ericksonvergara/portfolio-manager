import { describe, expect, it } from 'vitest'
import { isPasswordValid } from './passwordPolicy'

describe('isPasswordValid (RF004)', () => {
  it.each(['Abc#12', 'Secret#123', 'Pa$$w0rd1234'])(
    'accepts %s',
    (password) => {
      expect(isPasswordValid(password)).toBe(true)
    },
  )

  it.each([
    ['too short', 'Ab#1'],
    ['too long', 'Abcdef#123456'],
    ['no uppercase', 'secret#123'],
    ['no lowercase', 'SECRET#123'],
    ['no digit', 'Secret#abc'],
    ['no special character', 'Secret1234'],
  ])('rejects a password with %s', (_, password) => {
    expect(isPasswordValid(password)).toBe(false)
  })
})
