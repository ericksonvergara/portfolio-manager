import { isAxiosError } from 'axios'
import { useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { ConfirmDialog } from '../../features/admin/ConfirmDialog'
import {
  deleteProject,
  listAdminProjects,
  setProjectPublication,
  type AdminProject,
} from '../../features/admin/projects/adminProjectsApi'
import {
  ErrorState,
  LoadingState,
} from '../../features/admin/projects/AdminStates'
import { useAdminData } from '../../features/admin/projects/useAdminData'
import { STATUS_LABELS } from '../../features/landing/labels'

const PRIMARY_LINK =
  'inline-block rounded-lg bg-sky-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-sky-500 focus:outline-none focus:ring-2 focus:ring-sky-400 focus:ring-offset-2 focus:ring-offset-slate-950'

/**
 * Every project of the portfolio, active or not (HU015), with access to its edition (HU016),
 * its deletion after a confirmation (HU017) and its publication (HU018).
 */
export function AdminProjectsPage() {
  const content = useAdminData(listAdminProjects)
  const saved = useLocation().state as {
    created?: string
    updated?: string
  } | null
  const [removed, setRemoved] = useState<number[]>([])
  const [deleted, setDeleted] = useState<string | null>(null)
  const [pending, setPending] = useState<AdminProject | null>(null)
  const [busy, setBusy] = useState(false)
  const [deleteError, setDeleteError] = useState<string | null>(null)
  const [changed, setChanged] = useState<Record<number, AdminProject>>({})
  const [publishing, setPublishing] = useState<number | null>(null)
  const [publication, setPublication] = useState<{
    message: string
    failed: boolean
  } | null>(null)

  const projects =
    content.status === 'ready'
      ? content.data
          .filter((project) => !removed.includes(project.id))
          .map((project) => changed[project.id] ?? project)
      : []

  const togglePublication = async (project: AdminProject) => {
    const active = !project.active
    setPublishing(project.id)
    setPublication(null)
    try {
      const updated = await setProjectPublication(project.id, active)
      setChanged((current) => ({ ...current, [project.id]: updated }))
      setPublication({
        message: active
          ? `«${project.name}» está publicado en el portafolio.`
          : `«${project.name}» ya no aparece en el portafolio.`,
        failed: false,
      })
    } catch (error) {
      const noCover = isAxiosError(error) && error.response?.status === 409
      setPublication({
        message: noCover
          ? `«${project.name}» necesita una imagen principal para publicarse.`
          : 'No se pudo cambiar la publicación. Inténtalo de nuevo.',
        failed: true,
      })
    } finally {
      setPublishing(null)
    }
  }

  const confirmDelete = async () => {
    if (!pending) return
    setBusy(true)
    setDeleteError(null)
    try {
      await deleteProject(pending.id)
    } catch (error) {
      // Already deleted (for example in another tab): the outcome is the same.
      if (!isAxiosError(error) || error.response?.status !== 404) {
        setDeleteError('No se pudo eliminar el proyecto. Inténtalo de nuevo.')
        setBusy(false)
        return
      }
    }
    setRemoved((current) => [...current, pending.id])
    setDeleted(pending.name)
    setPending(null)
    setBusy(false)
  }

  const cancelDelete = () => {
    setPending(null)
    setDeleteError(null)
  }

  return (
    <section aria-labelledby="projects-title">
      <div className="mb-6 flex flex-wrap items-center justify-between gap-4">
        <h1 id="projects-title" className="text-2xl font-bold">
          Proyectos
        </h1>
        <Link to="/admin/proyectos/nuevo" className={PRIMARY_LINK}>
          Registrar proyecto
        </Link>
      </div>

      {publication && (
        <p
          role={publication.failed ? 'alert' : 'status'}
          className={`mb-4 rounded-lg border px-3 py-2 text-sm ${
            publication.failed
              ? 'border-amber-800 bg-amber-950 text-amber-200'
              : 'border-emerald-800 bg-emerald-950 text-emerald-200'
          }`}
        >
          {publication.message}
        </p>
      )}

      {!publication && (deleted || saved?.created || saved?.updated) && (
        <p
          role="status"
          className="mb-4 rounded-lg border border-emerald-800 bg-emerald-950 px-3 py-2 text-sm text-emerald-200"
        >
          {deleted
            ? `Proyecto «${deleted}» eliminado.`
            : saved?.created
              ? `Proyecto «${saved.created}» registrado. Queda inactivo hasta que tenga imagen principal.`
              : `Proyecto «${saved?.updated}» actualizado.`}
        </p>
      )}

      {content.status === 'loading' && (
        <LoadingState label="Cargando proyectos…" />
      )}
      {content.status === 'error' && (
        <ErrorState
          message="No se pudieron cargar los proyectos."
          retry={content.retry}
        />
      )}
      {content.status === 'ready' && projects.length === 0 && (
        <p className="rounded-lg border border-dashed border-slate-700 px-4 py-8 text-center text-slate-400">
          Todavía no hay proyectos. Registra el primero.
        </p>
      )}
      {projects.length > 0 && (
        <div className="overflow-x-auto rounded-lg border border-slate-800">
          <table className="w-full min-w-[640px] text-left text-sm">
            <caption className="sr-only">Proyectos del portafolio</caption>
            <thead className="bg-slate-900 text-slate-400">
              <tr>
                <th scope="col" className="px-4 py-3 font-medium">
                  Nombre
                </th>
                <th scope="col" className="px-4 py-3 font-medium">
                  Estado
                </th>
                <th scope="col" className="px-4 py-3 font-medium">
                  Publicación
                </th>
                <th scope="col" className="px-4 py-3 font-medium">
                  Orden
                </th>
                <th scope="col" className="px-4 py-3 font-medium">
                  Tecnologías
                </th>
                <th scope="col" className="px-4 py-3 font-medium">
                  <span className="sr-only">Acciones</span>
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800">
              {projects.map((project) => (
                <tr key={project.id}>
                  <th scope="row" className="px-4 py-3 font-medium text-white">
                    {project.name}
                    {project.featured && (
                      <span className="ml-2 rounded-full bg-amber-500/15 px-2 py-0.5 text-xs text-amber-300">
                        Destacado
                      </span>
                    )}
                  </th>
                  <td className="px-4 py-3 text-slate-300">
                    {STATUS_LABELS[project.status]}
                  </td>
                  <td className="px-4 py-3">
                    <button
                      type="button"
                      role="switch"
                      aria-checked={project.active}
                      aria-label={`Publicar ${project.name}`}
                      disabled={publishing === project.id}
                      onClick={() => void togglePublication(project)}
                      className="inline-flex items-center gap-2 rounded-full focus:outline-none focus:ring-2 focus:ring-sky-400 disabled:opacity-60"
                    >
                      <span
                        aria-hidden="true"
                        className={`relative h-5 w-9 rounded-full transition ${
                          project.active ? 'bg-emerald-500' : 'bg-slate-600'
                        }`}
                      >
                        <span
                          className={`absolute top-0.5 h-4 w-4 rounded-full bg-white transition ${
                            project.active ? 'left-4' : 'left-0.5'
                          }`}
                        />
                      </span>
                      <span
                        className={`text-xs ${
                          project.active ? 'text-emerald-300' : 'text-slate-300'
                        }`}
                      >
                        {project.active ? 'Activo' : 'Inactivo'}
                      </span>
                    </button>
                  </td>
                  <td className="px-4 py-3 text-slate-300">
                    {project.displayOrder}
                  </td>
                  <td className="px-4 py-3 text-slate-300">
                    {project.technologies.map((t) => t.name).join(', ')}
                  </td>
                  <td className="whitespace-nowrap px-4 py-3 text-right">
                    <Link
                      to={`/admin/proyectos/${project.id}/editar`}
                      aria-label={`Editar ${project.name}`}
                      className="rounded-lg border border-slate-700 px-3 py-1 text-xs font-semibold text-slate-200 hover:bg-slate-900 focus:outline-none focus:ring-2 focus:ring-sky-400"
                    >
                      Editar
                    </Link>
                    <button
                      type="button"
                      onClick={() => setPending(project)}
                      aria-label={`Eliminar ${project.name}`}
                      className="ml-2 rounded-lg border border-red-900 px-3 py-1 text-xs font-semibold text-red-300 hover:bg-red-950 focus:outline-none focus:ring-2 focus:ring-red-400"
                    >
                      Eliminar
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      {pending && (
        <ConfirmDialog
          title="Eliminar proyecto"
          message={`¿Eliminar «${pending.name}»? Se borrarán también su galería y sus datos. Esta acción no se puede deshacer.`}
          confirmLabel="Eliminar"
          busy={busy}
          error={deleteError}
          onConfirm={() => void confirmDelete()}
          onCancel={cancelDelete}
        />
      )}
    </section>
  )
}
