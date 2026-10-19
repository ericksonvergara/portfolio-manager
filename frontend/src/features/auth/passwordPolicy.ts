/**
 * RF004: 6-12 characters with at least one uppercase letter, one lowercase letter,
 * one digit and one special character. Mirrors PasswordPolicy in the backend.
 */
export const PASSWORD_POLICY_PATTERN =
  /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{6,12}$/

export const PASSWORD_POLICY_MESSAGE =
  'Debe tener entre 6 y 12 caracteres, con mayúscula, minúscula, número y carácter especial.'

export function isPasswordValid(password: string): boolean {
  return PASSWORD_POLICY_PATTERN.test(password)
}
