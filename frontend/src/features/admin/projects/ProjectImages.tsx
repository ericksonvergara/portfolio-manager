import { useRef, useState } from 'react'
import { ConfirmDialog } from '../ConfirmDialog'
import { ImagePreview } from './ImagePreview'
import {
  addGalleryImage,
  removeCover,
  removeGalleryImage,
  uploadCover,
  type AdminGalleryImage,
  type AdminProject,
} from './adminProjectsApi'
import {
  checkImageFile,
  IMAGE_ACCEPT,
  imageErrorMessage,
  MAX_IMAGE_MB,
} from './imageRules'

const BUTTON =
  'rounded-lg px-3 py-2 text-sm font-semibold transition focus:outline-none focus:ring-2 focus:ring-sky-400 disabled:cursor-not-allowed disabled:opacity-60'
const FILE_INPUT =
  'block w-full text-sm text-slate-300 file:mr-3 file:rounded-lg file:border-0 file:bg-slate-800 file:px-3 file:py-2 file:text-sm file:font-semibold file:text-slate-100 hover:file:bg-slate-700'

interface ProjectImagesProps {
  project: AdminProject
  onChange: (project: AdminProject) => void
}

type Message = { text: string; failed: boolean } | null

/**
 * Cover image and gallery of a project (HU019). A selected file is previewed before it is saved
 * (HU020); the backend validates the format, the real content and the size of every file (RF011).
 */
export function ProjectImages({ project, onChange }: ProjectImagesProps) {
  const [message, setMessage] = useState<Message>(null)
  const [busy, setBusy] = useState(false)
  const [cover, setCover] = useState<File | null>(null)
  const [image, setImage] = useState<File | null>(null)
  const [altText, setAltText] = useState('')
  const [pending, setPending] = useState<AdminGalleryImage | null>(null)
  const coverInput = useRef<HTMLInputElement>(null)
  const imageInput = useRef<HTMLInputElement>(null)

  const run = async (action: () => Promise<AdminProject>, done: string) => {
    setBusy(true)
    setMessage(null)
    try {
      onChange(await action())
      setMessage({ text: done, failed: false })
      return true
    } catch (error) {
      setMessage({ text: imageErrorMessage(error), failed: true })
      return false
    } finally {
      setBusy(false)
    }
  }

  const choose = (
    file: File | undefined,
    keep: (file: File | null) => void,
  ) => {
    const problem = file ? checkImageFile(file) : null
    setMessage(problem ? { text: problem, failed: true } : null)
    keep(file && !problem ? file : null)
  }

  const discardCover = () => {
    setCover(null)
    if (coverInput.current) coverInput.current.value = ''
  }

  const discardImage = () => {
    setImage(null)
    if (imageInput.current) imageInput.current.value = ''
  }

  const saveCover = async () => {
    if (!cover) return
    const replaced = project.coverImageUrl !== null
    if (
      await run(
        () => uploadCover(project.id, cover),
        replaced
          ? 'Imagen principal reemplazada.'
          : 'Imagen principal guardada.',
      )
    ) {
      discardCover()
    }
  }

  const saveImage = async () => {
    if (!image) return
    if (
      await run(
        () => addGalleryImage(project.id, image, altText),
        'Imagen agregada a la galería.',
      )
    ) {
      discardImage()
      setAltText('')
    }
  }

  const deleteImage = async () => {
    if (!pending) return
    const removed = pending
    await run(
      () => removeGalleryImage(project.id, removed.id),
      'Imagen eliminada de la galería.',
    )
    setPending(null)
  }

  return (
    <section aria-labelledby="images-title" className="mt-10 max-w-3xl">
      <h2 id="images-title" className="text-xl font-bold">
        Imágenes
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

      <div className="mt-6 rounded-lg border border-slate-800 p-4">
        <h3 className="font-semibold">Imagen principal</h3>
        {project.coverImageUrl ? (
          <img
            src={project.coverImageUrl}
            alt={`Imagen principal de ${project.name}`}
            className="mt-3 max-h-64 rounded-lg border border-slate-800 object-contain"
          />
        ) : (
          <p className="mt-3 rounded-lg border border-dashed border-slate-700 px-4 py-6 text-center text-sm text-slate-400">
            Sin imagen principal. El proyecto no se puede publicar hasta que
            tenga una.
          </p>
        )}

        <label
          htmlFor="cover-file"
          className="mt-4 mb-1 block text-sm font-medium text-slate-200"
        >
          {project.coverImageUrl
            ? 'Nueva imagen principal'
            : 'Archivo de la imagen principal'}
        </label>
        <input
          ref={coverInput}
          id="cover-file"
          type="file"
          accept={IMAGE_ACCEPT}
          className={FILE_INPUT}
          onChange={(event) => choose(event.target.files?.[0], setCover)}
        />
        {cover && (
          <ImagePreview
            file={cover}
            label={
              project.coverImageUrl
                ? 'Vista previa de la nueva imagen principal'
                : 'Vista previa de la imagen principal'
            }
            disabled={busy}
            onReplace={() => coverInput.current?.click()}
            onDiscard={discardCover}
          />
        )}
        <div className="mt-3 flex flex-wrap gap-3">
          <button
            type="button"
            disabled={!cover || busy}
            onClick={() => void saveCover()}
            className={`${BUTTON} bg-sky-600 text-white hover:bg-sky-500`}
          >
            {project.coverImageUrl ? 'Reemplazar imagen' : 'Subir imagen'}
          </button>
          {project.coverImageUrl && (
            <button
              type="button"
              disabled={busy || project.active}
              onClick={() =>
                void run(
                  () => removeCover(project.id),
                  'Imagen principal eliminada.',
                )
              }
              aria-describedby={
                project.active ? 'cover-active-hint' : undefined
              }
              className={`${BUTTON} border border-slate-700 text-slate-200 hover:bg-slate-900`}
            >
              Quitar imagen
            </button>
          )}
        </div>
        {project.coverImageUrl && project.active && (
          <p id="cover-active-hint" className="mt-2 text-xs text-slate-500">
            El proyecto está publicado: para quitar su imagen principal, primero
            desactívalo en el listado.
          </p>
        )}
      </div>

      <div className="mt-6 rounded-lg border border-slate-800 p-4">
        <h3 className="font-semibold">Galería</h3>
        {project.gallery.length === 0 ? (
          <p className="mt-3 text-sm text-slate-400">La galería está vacía.</p>
        ) : (
          <ul className="mt-3 grid gap-4 sm:grid-cols-2">
            {project.gallery.map((item, index) => (
              <li
                key={item.id}
                className="rounded-lg border border-slate-800 p-2"
              >
                <img
                  src={item.url}
                  alt={
                    item.altText ??
                    `Imagen ${index + 1} de la galería de ${project.name}`
                  }
                  className="h-40 w-full rounded object-cover"
                />
                <div className="mt-2 flex items-center justify-between gap-2">
                  <p className="text-xs text-slate-400">
                    {item.altText ?? 'Sin texto alternativo'}
                  </p>
                  <button
                    type="button"
                    disabled={busy}
                    onClick={() => setPending(item)}
                    aria-label={`Eliminar imagen ${index + 1} de la galería`}
                    className="rounded-lg border border-red-900 px-2 py-1 text-xs font-semibold text-red-300 hover:bg-red-950 focus:outline-none focus:ring-2 focus:ring-red-400"
                  >
                    Eliminar
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}

        <div className="mt-4 grid gap-3 sm:grid-cols-2">
          <div>
            <label
              htmlFor="gallery-file"
              className="mb-1 block text-sm font-medium text-slate-200"
            >
              Nueva imagen de la galería
            </label>
            <input
              ref={imageInput}
              id="gallery-file"
              type="file"
              accept={IMAGE_ACCEPT}
              className={FILE_INPUT}
              onChange={(event) => choose(event.target.files?.[0], setImage)}
            />
          </div>
          <div>
            <label
              htmlFor="gallery-alt"
              className="mb-1 block text-sm font-medium text-slate-200"
            >
              Texto alternativo{' '}
              <span className="text-slate-500">(opcional)</span>
            </label>
            <input
              id="gallery-alt"
              maxLength={200}
              value={altText}
              onChange={(event) => setAltText(event.target.value)}
              placeholder="Qué muestra la imagen"
              className="w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-white outline-none focus:border-sky-500 focus:ring-2 focus:ring-sky-500/40"
            />
          </div>
        </div>
        {image && (
          <ImagePreview
            file={image}
            label="Vista previa de la nueva imagen de la galería"
            disabled={busy}
            onReplace={() => imageInput.current?.click()}
            onDiscard={discardImage}
          />
        )}
        <button
          type="button"
          disabled={!image || busy}
          onClick={() => void saveImage()}
          className={`${BUTTON} mt-3 bg-sky-600 text-white hover:bg-sky-500`}
        >
          Agregar a la galería
        </button>
      </div>

      {pending && (
        <ConfirmDialog
          title="Eliminar imagen"
          message="¿Eliminar esta imagen de la galería? El archivo se borrará y no se puede deshacer."
          confirmLabel="Eliminar"
          busy={busy}
          error={null}
          onConfirm={() => void deleteImage()}
          onCancel={() => setPending(null)}
        />
      )}
    </section>
  )
}
