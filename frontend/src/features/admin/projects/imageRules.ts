import { isAxiosError } from 'axios'

/** Same rules as the backend (RF011); the backend is the one that decides. */
export const MAX_IMAGE_MB = 5
const MAX_IMAGE_BYTES = MAX_IMAGE_MB * 1024 * 1024
const ALLOWED_TYPES = ['image/jpeg', 'image/png']
const ALLOWED_EXTENSIONS = /\.(jpe?g|png)$/i

export const IMAGE_ACCEPT = '.jpg,.jpeg,.png,image/jpeg,image/png'

/** Checks a selected file before uploading it; returns the message to show, or null. */
export function checkImageFile(file: File): string | null {
  if (
    !ALLOWED_TYPES.includes(file.type) ||
    !ALLOWED_EXTENSIONS.test(file.name)
  ) {
    return 'Formato no permitido. Usa una imagen JPG, JPEG o PNG.'
  }
  if (file.size > MAX_IMAGE_BYTES) {
    return `La imagen supera el tamaño máximo de ${MAX_IMAGE_MB} MB.`
  }
  if (file.size === 0) {
    return 'El archivo está vacío.'
  }
  return null
}

/** The message for a failed image request. */
export function imageErrorMessage(error: unknown): string {
  const status = isAxiosError(error) ? error.response?.status : undefined
  if (status === 413) {
    return `La imagen supera el tamaño máximo de ${MAX_IMAGE_MB} MB.`
  }
  if (status === 400) {
    return 'El archivo no es una imagen JPG o PNG válida.'
  }
  if (status === 409) {
    return 'El proyecto está publicado: desactívalo antes de quitar su imagen principal.'
  }
  return 'No se pudo guardar la imagen. Inténtalo de nuevo.'
}
