import { useEffect, useMemo, useState } from 'react'
import { formatFileSize } from './imageRules'

interface ImagePreviewProps {
  file: File
  label: string
  disabled: boolean
  onReplace: () => void
  onDiscard: () => void
}

/**
 * Shows a selected image before it is saved (HU020). The browser reads the file locally, so
 * nothing is uploaded, and the original is displayed as it is: no resizing or recompression,
 * only fitted to the available space.
 */
export function ImagePreview({
  file,
  label,
  disabled,
  onReplace,
  onDiscard,
}: ImagePreviewProps) {
  const url = useMemo(() => URL.createObjectURL(file), [file])
  const [size, setSize] = useState<{ width: number; height: number } | null>(
    null,
  )

  useEffect(() => () => URL.revokeObjectURL(url), [url])

  return (
    <figure className="mt-4 rounded-lg border border-sky-900 bg-slate-900/60 p-3">
      <img
        src={url}
        alt={label}
        onLoad={(event) =>
          setSize({
            width: event.currentTarget.naturalWidth,
            height: event.currentTarget.naturalHeight,
          })
        }
        className="mx-auto max-h-72 max-w-full rounded object-contain"
      />
      <figcaption className="mt-2 text-xs text-slate-400">
        <span className="font-semibold text-slate-200">Vista previa</span> ·{' '}
        {file.name} · {formatFileSize(file.size)}
        {size && ` · ${size.width} × ${size.height} px`}. Aún no se ha guardado.
      </figcaption>
      <div className="mt-3 flex flex-wrap gap-2">
        <button
          type="button"
          disabled={disabled}
          onClick={onReplace}
          className="rounded-lg border border-slate-700 px-3 py-1 text-xs font-semibold text-slate-200 hover:bg-slate-800 focus:outline-none focus:ring-2 focus:ring-sky-400 disabled:opacity-60"
        >
          Cambiar imagen
        </button>
        <button
          type="button"
          disabled={disabled}
          onClick={onDiscard}
          className="rounded-lg border border-slate-700 px-3 py-1 text-xs font-semibold text-slate-200 hover:bg-slate-800 focus:outline-none focus:ring-2 focus:ring-sky-400 disabled:opacity-60"
        >
          Descartar
        </button>
      </div>
    </figure>
  )
}
