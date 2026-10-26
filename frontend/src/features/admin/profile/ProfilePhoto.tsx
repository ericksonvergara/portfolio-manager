import { isAxiosError } from 'axios'
import { useRef, useState } from 'react'
import type { Profile } from '../../landing/landingApi'
import { ConfirmDialog } from '../ConfirmDialog'
import { ImagePreview } from '../projects/ImagePreview'
import {
  checkImageFile,
  IMAGE_ACCEPT,
  imageErrorMessage,
  MAX_IMAGE_MB,
} from '../projects/imageRules'
import { removeProfilePhoto, uploadProfilePhoto } from './adminProfileApi'

const BUTTON =
  'rounded-lg px-3 py-2 text-sm font-semibold transition focus:outline-none focus:ring-2 focus:ring-sky-400 disabled:cursor-not-allowed disabled:opacity-60'
const FILE_INPUT =
  'block w-full text-sm text-slate-300 file:mr-3 file:rounded-lg file:border-0 file:bg-slate-800 file:px-3 file:py-2 file:text-sm file:font-semibold file:text-slate-100 hover:file:bg-slate-700'

type Message = { text: string; failed: boolean } | null

interface ProfilePhotoProps {
  /** Null while the profile has not been saved: the photo needs a profile. */
  profile: Profile | null
  onChange: (profile: Profile) => void
}

/** The profile photo (HU023), previewed before it is uploaded like the project images (HU020). */
export function ProfilePhoto({ profile, onChange }: ProfilePhotoProps) {
  const [message, setMessage] = useState<Message>(null)
  const [busy, setBusy] = useState(false)
  const [file, setFile] = useState<File | null>(null)
  const [confirming, setConfirming] = useState(false)
  const input = useRef<HTMLInputElement>(null)

  const discard = () => {
    setFile(null)
    if (input.current) input.current.value = ''
  }

  const run = async (action: () => Promise<Profile>, done: string) => {
    setBusy(true)
    setMessage(null)
    try {
      onChange(await action())
      setMessage({ text: done, failed: false })
      return true
    } catch (error) {
      const notSaved = isAxiosError(error) && error.response?.status === 404
      setMessage({
        text: notSaved
          ? 'Guarda el perfil antes de subir la fotografía.'
          : imageErrorMessage(error),
        failed: true,
      })
      return false
    } finally {
      setBusy(false)
    }
  }

  const choose = (selected: File | undefined) => {
    const problem = selected ? checkImageFile(selected) : null
    setMessage(problem ? { text: problem, failed: true } : null)
    setFile(selected && !problem ? selected : null)
  }

  const save = async () => {
    if (!file) return
    const replaced = profile?.photoUrl != null
    if (
      await run(
        () => uploadProfilePhoto(file),
        replaced ? 'Fotografía reemplazada.' : 'Fotografía guardada.',
      )
    ) {
      discard()
    }
  }

  const remove = async () => {
    await run(removeProfilePhoto, 'Fotografía eliminada.')
    setConfirming(false)
  }

  return (
    <section aria-labelledby="photo-title" className="mt-10 max-w-3xl">
      <h2 id="photo-title" className="text-xl font-bold">
        Fotografía
      </h2>
      <p className="mt-1 text-sm text-slate-400">
        JPG, JPEG o PNG, hasta {MAX_IMAGE_MB} MB. Los cambios se guardan al
        instante.
      </p>

      {message && (
        <p
          role={message.failed ? 'alert' : 'status'}
          className={`mt-4 rounded-lg border px-3 py-2 text-sm ${
            message.failed
              ? 'border-red-800 bg-red-950 text-red-300'
              : 'border-emerald-800 bg-emerald-950 text-emerald-200'
          }`}
        >
          {message.text}
        </p>
      )}

      {!profile ? (
        <p className="mt-4 rounded-lg border border-dashed border-slate-700 px-4 py-6 text-center text-sm text-slate-400">
          Guarda el perfil para poder agregar una fotografía.
        </p>
      ) : (
        <div className="mt-4 rounded-lg border border-slate-800 p-4">
          {profile.photoUrl ? (
            <img
              src={profile.photoUrl}
              alt={`Fotografía de ${profile.fullName}`}
              className="h-32 w-32 rounded-full border border-slate-800 object-cover"
            />
          ) : (
            <p className="rounded-lg border border-dashed border-slate-700 px-4 py-6 text-center text-sm text-slate-400">
              Sin fotografía. El portafolio muestra tus iniciales.
            </p>
          )}

          <label
            htmlFor="photo-file"
            className="mt-4 mb-1 block text-sm font-medium text-slate-200"
          >
            {profile.photoUrl ? 'Nueva fotografía' : 'Archivo de la fotografía'}
          </label>
          <input
            ref={input}
            id="photo-file"
            type="file"
            accept={IMAGE_ACCEPT}
            className={FILE_INPUT}
            onChange={(event) => choose(event.target.files?.[0])}
          />
          {file && (
            <ImagePreview
              file={file}
              label="Vista previa de la fotografía"
              disabled={busy}
              onReplace={() => input.current?.click()}
              onDiscard={discard}
            />
          )}
          <div className="mt-3 flex flex-wrap gap-3">
            <button
              type="button"
              disabled={!file || busy}
              onClick={() => void save()}
              className={`${BUTTON} bg-sky-600 text-white hover:bg-sky-500`}
            >
              {profile.photoUrl ? 'Reemplazar fotografía' : 'Subir fotografía'}
            </button>
            {profile.photoUrl && (
              <button
                type="button"
                disabled={busy}
                onClick={() => setConfirming(true)}
                className={`${BUTTON} border border-red-800 text-red-300 hover:bg-red-950`}
              >
                Quitar fotografía
              </button>
            )}
          </div>
        </div>
      )}

      {confirming && (
        <ConfirmDialog
          title="Quitar la fotografía"
          message="El portafolio mostrará tus iniciales en su lugar."
          confirmLabel="Quitar"
          busy={busy}
          error={null}
          onConfirm={() => void remove()}
          onCancel={() => setConfirming(false)}
        />
      )}
    </section>
  )
}
