import type { ReactNode } from 'react'
import { getDashboard } from '../../features/admin/dashboard/adminDashboardApi'
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
      {content.status === 'ready' && (
        <dl className="grid gap-4 sm:grid-cols-2">
          <Indicator label="Proyectos">
            {content.data.totalProjects.toLocaleString('es')}
          </Indicator>
          <Indicator label="Tecnologías">
            {content.data.totalTechnologies.toLocaleString('es')}
          </Indicator>
          <Indicator
            label="Último proyecto registrado"
            detail={
              content.data.latestProject &&
              DATE_TIME.format(
                new Date(content.data.latestProject.registeredAt),
              )
            }
          >
            {content.data.latestProject?.name ?? 'Ninguno todavía'}
          </Indicator>
          <Indicator label="Último acceso" detail="Tu sesión anterior a esta.">
            {content.data.lastAccessAt
              ? DATE_TIME.format(new Date(content.data.lastAccessAt))
              : 'Este es tu primer acceso'}
          </Indicator>
        </dl>
      )}
    </section>
  )
}
