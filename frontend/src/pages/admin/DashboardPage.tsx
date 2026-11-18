import type { ReactNode } from 'react'
import {
  getDashboard,
  type DashboardSummary,
} from '../../features/admin/dashboard/adminDashboardApi'
import {
  ErrorState,
  LoadingState,
} from '../../features/admin/projects/AdminStates'
import { useAdminData } from '../../features/admin/projects/useAdminData'

const DATE_TIME = new Intl.DateTimeFormat('es', {
  dateStyle: 'medium',
  timeStyle: 'short',
})

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
  const content = useAdminData(getDashboard)

  return (
    <section aria-labelledby="dashboard-title" className="max-w-4xl">
      <h1 id="dashboard-title" className="mb-6 text-2xl font-bold">
        Inicio
      </h1>

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
    </div>
  )
}
