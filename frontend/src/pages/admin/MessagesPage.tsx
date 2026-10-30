import { isAxiosError } from 'axios'
import { useState } from 'react'
import { ConfirmDialog } from '../../features/admin/ConfirmDialog'
import {
  deleteMessage,
  listMessages,
  markMessageRead,
  type ContactMessage,
} from '../../features/admin/messages/adminMessagesApi'
import {
  ErrorState,
  LoadingState,
} from '../../features/admin/projects/AdminStates'
import { useAdminData } from '../../features/admin/projects/useAdminData'

const DATE_FORMAT = new Intl.DateTimeFormat('es', {
  dateStyle: 'medium',
  timeStyle: 'short',
})

const BUTTON =
  'rounded-lg px-3 py-1.5 text-sm font-semibold transition focus:outline-none focus-visible:ring-2 focus-visible:ring-sky-400 disabled:cursor-not-allowed disabled:opacity-60'

/** The inbox of the contact form (HU006): read, answer by email and delete the messages. */
export function MessagesPage() {
  const content = useAdminData(listMessages)
  const [changed, setChanged] = useState<ContactMessage[] | null>(null)
  const [pending, setPending] = useState<ContactMessage | null>(null)
  const [busy, setBusy] = useState(false)
  const [deleteError, setDeleteError] = useState<string | null>(null)
  const [notice, setNotice] = useState<{
    text: string
    failed: boolean
  } | null>(null)

  const messages = changed ?? (content.status === 'ready' ? content.data : [])
  const unread = messages.filter((m) => !m.read).length

  const markRead = async (message: ContactMessage) => {
    setNotice(null)
    try {
      const updated = await markMessageRead(message.id)
      setChanged(messages.map((m) => (m.id === updated.id ? updated : m)))
    } catch {
      setNotice({
        text: 'No se pudo marcar el mensaje como leído. Inténtalo de nuevo.',
        failed: true,
      })
    }
  }

  const confirmDelete = async () => {
    if (!pending) return
    setBusy(true)
    setDeleteError(null)
    try {
      await deleteMessage(pending.id)
    } catch (error) {
      // 404: someone else already deleted it, which is what was wanted.
      if (!isAxiosError(error) || error.response?.status !== 404) {
        setDeleteError('No se pudo eliminar el mensaje. Inténtalo de nuevo.')
        setBusy(false)
        return
      }
    }
    setChanged(messages.filter((m) => m.id !== pending.id))
    setNotice({ text: `Mensaje de ${pending.name} eliminado.`, failed: false })
    setPending(null)
    setBusy(false)
  }

  return (
    <section aria-labelledby="messages-title" className="max-w-4xl">
      <h1 id="messages-title" className="mb-2 text-2xl font-bold">
        Mensajes
      </h1>
      <p className="mb-6 text-sm text-slate-400">
        Lo que los visitantes envían desde el formulario de contacto del
        portafolio. Además te llega un aviso al correo de contacto del perfil.
      </p>

      {notice && (
        <p
          role={notice.failed ? 'alert' : 'status'}
          className={`mb-4 rounded-lg border px-3 py-2 text-sm ${
            notice.failed
              ? 'border-red-800 bg-red-950 text-red-300'
              : 'border-emerald-800 bg-emerald-950 text-emerald-200'
          }`}
        >
          {notice.text}
        </p>
      )}

      {content.status === 'loading' && (
        <LoadingState label="Cargando mensajes…" />
      )}
      {content.status === 'error' && (
        <ErrorState
          message="No se pudieron cargar los mensajes."
          retry={content.retry}
        />
      )}
      {content.status === 'ready' && messages.length === 0 && (
        <p className="rounded-lg border border-dashed border-slate-700 px-4 py-6 text-center text-sm text-slate-400">
          Aún no has recibido mensajes.
        </p>
      )}
      {content.status === 'ready' && messages.length > 0 && (
        <>
          <p className="mb-4 text-sm text-slate-300">
            {messages.length === 1
              ? '1 mensaje'
              : `${messages.length} mensajes`}
            {unread > 0 && ` · ${unread} sin leer`}
          </p>
          <ul className="space-y-4">
            {messages.map((message) => (
              <li key={message.id}>
                <article
                  aria-label={`Mensaje de ${message.name}`}
                  className={`rounded-lg border p-4 ${
                    message.read
                      ? 'border-slate-800 bg-slate-900/40'
                      : 'border-sky-800 bg-sky-950/30'
                  }`}
                >
                  <header className="flex flex-wrap items-baseline justify-between gap-2">
                    <div className="min-w-0">
                      <h2 className="font-semibold text-white">
                        {message.name}
                        {!message.read && (
                          <span className="ml-2 rounded-full bg-sky-500/15 px-2 py-0.5 text-xs font-semibold text-sky-300">
                            Sin leer
                          </span>
                        )}
                      </h2>
                      <a
                        href={`mailto:${message.email}`}
                        className="text-sm break-all text-sky-400 hover:text-sky-300"
                      >
                        {message.email}
                      </a>
                    </div>
                    <time
                      dateTime={message.receivedAt}
                      className="text-xs text-slate-400"
                    >
                      {DATE_FORMAT.format(new Date(message.receivedAt))}
                    </time>
                  </header>
                  <p className="mt-3 text-sm whitespace-pre-line text-slate-200">
                    {message.message}
                  </p>
                  <div className="mt-4 flex flex-wrap gap-2">
                    <a
                      href={`mailto:${message.email}?subject=${encodeURIComponent('Re: tu mensaje en el portafolio')}`}
                      className={`${BUTTON} bg-sky-600 text-white hover:bg-sky-500`}
                    >
                      Responder
                    </a>
                    {!message.read && (
                      <button
                        type="button"
                        onClick={() => void markRead(message)}
                        className={`${BUTTON} border border-slate-700 text-slate-200 hover:bg-slate-800`}
                      >
                        Marcar como leído
                      </button>
                    )}
                    <button
                      type="button"
                      onClick={() => {
                        setDeleteError(null)
                        setPending(message)
                      }}
                      className={`${BUTTON} border border-red-800 text-red-300 hover:bg-red-950`}
                    >
                      Eliminar
                    </button>
                  </div>
                </article>
              </li>
            ))}
          </ul>
        </>
      )}

      {pending && (
        <ConfirmDialog
          title="Eliminar mensaje"
          message={`Se eliminará el mensaje de ${pending.name}. Esta acción no se puede deshacer.`}
          confirmLabel="Eliminar"
          busy={busy}
          error={deleteError}
          onConfirm={() => void confirmDelete()}
          onCancel={() => setPending(null)}
        />
      )}
    </section>
  )
}
