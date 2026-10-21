import { Link, useLocation } from 'react-router-dom'
import { listAdminProjects } from '../../features/admin/projects/adminProjectsApi'
import {
  ErrorState,
  LoadingState,
} from '../../features/admin/projects/AdminStates'
import { useAdminData } from '../../features/admin/projects/useAdminData'
import { STATUS_LABELS } from '../../features/landing/labels'

const PRIMARY_LINK =
  'inline-block rounded-lg bg-sky-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-sky-500 focus:outline-none focus:ring-2 focus:ring-sky-400 focus:ring-offset-2 focus:ring-offset-slate-950'

/** Every project of the portfolio, active or not (HU015), with access to its edition (HU016). */
export function AdminProjectsPage() {
  const content = useAdminData(listAdminProjects)
  const saved = useLocation().state as {
    created?: string
    updated?: string
  } | null

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

      {(saved?.created || saved?.updated) && (
        <p
          role="status"
          className="mb-4 rounded-lg border border-emerald-800 bg-emerald-950 px-3 py-2 text-sm text-emerald-200"
        >
          {saved.created
            ? `Proyecto «${saved.created}» registrado. Queda inactivo hasta que tenga imagen principal.`
            : `Proyecto «${saved.updated}» actualizado.`}
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
      {content.status === 'ready' && content.data.length === 0 && (
        <p className="rounded-lg border border-dashed border-slate-700 px-4 py-8 text-center text-slate-400">
          Todavía no hay proyectos. Registra el primero.
        </p>
      )}
      {content.status === 'ready' && content.data.length > 0 && (
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
              {content.data.map((project) => (
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
                    <span
                      className={`rounded-full px-2 py-0.5 text-xs ${
                        project.active
                          ? 'bg-emerald-500/15 text-emerald-300'
                          : 'bg-slate-700/50 text-slate-300'
                      }`}
                    >
                      {project.active ? 'Activo' : 'Inactivo'}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-slate-300">
                    {project.displayOrder}
                  </td>
                  <td className="px-4 py-3 text-slate-300">
                    {project.technologies.map((t) => t.name).join(', ')}
                  </td>
                  <td className="px-4 py-3 text-right">
                    <Link
                      to={`/admin/proyectos/${project.id}/editar`}
                      aria-label={`Editar ${project.name}`}
                      className="rounded-lg border border-slate-700 px-3 py-1 text-xs font-semibold text-slate-200 hover:bg-slate-900 focus:outline-none focus:ring-2 focus:ring-sky-400"
                    >
                      Editar
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  )
}
