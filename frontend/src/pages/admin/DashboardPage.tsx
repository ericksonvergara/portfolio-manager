import type { ReactNode } from 'react'
import { Link } from 'react-router-dom'
import type { DashboardSummary } from '../../features/admin/dashboard/adminDashboardApi'
import { useDashboard } from '../../features/admin/dashboard/useDashboard'
import {
  ErrorState,
  LoadingState,
} from '../../features/admin/projects/AdminStates'

const DATE_TIME = new Intl.DateTimeFormat('es', {
  dateStyle: 'medium',
  timeStyle: 'short',
})

const TIME = new Intl.DateTimeFormat('es', { timeStyle: 'short' })

function Indicator({
  label,
  children,
  detail,
}: {
  label: string
  children: ReactNode
  detail?: ReactNode
}) {
  return (
    <div className="rounded-lg border border-slate-800 bg-slate-900/40 p-4">
      <dt className="text-sm text-slate-400">{label}</dt>
      <dd className="mt-1 text-2xl font-bold break-words text-white">
        {children}
      </dd>
      {detail && <dd className="mt-1 text-sm text-slate-400">{detail}</dd>}
    </div>
  )
}

/** The first screen of the admin panel: the general state of the portfolio (HU012). */
export function DashboardPage() {
  const content = useDashboard()

  return (
    <section aria-labelledby="dashboard-title" className="max-w-4xl">
      <div className="mb-6 flex flex-wrap items-baseline justify-between gap-2">
        <h1 id="dashboard-title" className="text-2xl font-bold">
          Inicio
        </h1>
        {content.status === 'ready' && (
          <p
            className={`text-xs ${content.stale ? 'text-amber-300' : 'text-slate-500'}`}
          >
            {content.stale
              ? `No se pudo actualizar: datos de las ${TIME.format(content.updatedAt)}.`
              : `Actualizado a las ${TIME.format(content.updatedAt)} · se actualiza cada minuto.`}
          </p>
        )}
      </div>

      {content.status === 'loading' && (
        <LoadingState label="Cargando indicadores…" />
      )}
      {content.status === 'error' && (
        <ErrorState
          message="No se pudieron cargar los indicadores."
          retry={content.retry}
        />
      )}
      {content.status === 'ready' && <Indicators summary={content.data} />}
    </section>
  )
}

function Indicators({ summary }: { summary: DashboardSummary }) {
  const { latestProject, latestPublishedProject, visits } = summary
  const mostVisited = visits.mostVisitedProject

  return (
    <div className="space-y-8">
      <section aria-labelledby="dashboard-portfolio">
        <h2 id="dashboard-portfolio" className="mb-3 text-lg font-semibold">
          Portafolio
        </h2>
        <dl className="grid gap-4 sm:grid-cols-2">
          <Indicator label="Proyectos">
            {summary.totalProjects.toLocaleString('es')}
          </Indicator>
          <Indicator label="Tecnologías">
            {summary.totalTechnologies.toLocaleString('es')}
          </Indicator>
          <Indicator
            label="Último proyecto registrado"
            detail={
              latestProject &&
              DATE_TIME.format(new Date(latestProject.registeredAt))
            }
          >
            {latestProject?.name ?? 'Ninguno todavía'}
          </Indicator>
          <Indicator
            label="Último proyecto publicado"
            detail={
              latestPublishedProject &&
              DATE_TIME.format(new Date(latestPublishedProject.publishedAt))
            }
          >
            {latestPublishedProject?.name ?? 'Ninguno todavía'}
          </Indicator>
          <Indicator label="Último acceso" detail="Tu sesión anterior a esta.">
            {summary.lastAccessAt
              ? DATE_TIME.format(new Date(summary.lastAccessAt))
              : 'Este es tu primer acceso'}
          </Indicator>
        </dl>
      </section>

      <section aria-labelledby="dashboard-visits">
        <h2 id="dashboard-visits" className="mb-1 text-lg font-semibold">
          Visitas
        </h2>
        <p className="mb-3 text-sm text-slate-400">
          Aperturas del portafolio y de sus proyectos, sin contar las recargas.
        </p>
        <dl className="grid gap-4 sm:grid-cols-2">
          <Indicator label="Visitas totales">
            {visits.total.toLocaleString('es')}
          </Indicator>
          <Indicator label="Visitas de hoy">
            {visits.today.toLocaleString('es')}
          </Indicator>
          <Indicator label="Visitas del mes">
            {visits.thisMonth.toLocaleString('es')}
          </Indicator>
          <Indicator
            label="Proyecto más visto"
            detail={
              mostVisited &&
              (mostVisited.visits === 1
                ? '1 visita'
                : `${mostVisited.visits.toLocaleString('es')} visitas`)
            }
          >
            {mostVisited?.name ?? 'Ninguno todavía'}
          </Indicator>
        </dl>
      </section>

      <Ranking projects={visits.ranking} />
    </div>
  )
}

/** The projects with the most visits to their detail (HU014). */
function Ranking({
  projects,
}: {
  projects: DashboardSummary['visits']['ranking']
}) {
  return (
    <section aria-labelledby="dashboard-ranking">
      <h2 id="dashboard-ranking" className="mb-3 text-lg font-semibold">
        Proyectos más visitados
      </h2>
      {projects.length === 0 ? (
        <p className="rounded-lg border border-dashed border-slate-700 px-4 py-6 text-center text-sm text-slate-400">
          Aún no hay visitas a los proyectos.
        </p>
      ) : (
        <div className="overflow-x-auto rounded-lg border border-slate-800">
          <table className="w-full min-w-[420px] text-left text-sm">
            <caption className="sr-only">
              Top {projects.length} de proyectos por visitas a su detalle
            </caption>
            <thead className="bg-slate-900 text-slate-400">
              <tr>
                <th scope="col" className="w-16 px-4 py-3 font-medium">
                  Posición
                </th>
                <th scope="col" className="px-4 py-3 font-medium">
                  Proyecto
                </th>
                <th scope="col" className="px-4 py-3 text-right font-medium">
                  Visitas
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800">
              {projects.map((project, index) => (
                <tr key={project.id}>
                  <td className="px-4 py-3 text-slate-400 tabular-nums">
                    {index + 1}
                  </td>
                  <th scope="row" className="px-4 py-3 font-medium">
                    <Link
                      to={`/admin/proyectos/${project.id}/editar`}
                      className="text-sky-400 hover:text-sky-300 focus:outline-none focus-visible:ring-2 focus-visible:ring-sky-400"
                    >
                      {project.name}
                    </Link>
                    {!project.published && (
                      <span className="ml-2 rounded-full bg-slate-700/60 px-2 py-0.5 text-xs font-normal text-slate-300">
                        Oculto
                      </span>
                    )}
                  </th>
                  <td className="px-4 py-3 text-right text-slate-200 tabular-nums">
                    {project.visits.toLocaleString('es')}
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
