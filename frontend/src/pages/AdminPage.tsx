import { useAuth } from '../features/auth/useAuth'

/**
 * Placeholder of the admin panel; the dashboard arrives in later sprints.
 */
export function AdminPage() {
  const { session, logout } = useAuth()

  return (
    <main className="min-h-screen bg-slate-950 px-6 py-10 text-white">
      <header className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold">Panel administrativo</h1>
          <p className="mt-2 text-slate-400">
            Bienvenido, {session?.user.name || session?.user.email}.
          </p>
        </div>
        <button
          type="button"
          onClick={() => void logout()}
          className="rounded-lg border border-slate-700 px-4 py-2 text-sm font-semibold text-slate-200 transition hover:border-slate-500 hover:bg-slate-900 focus:outline-none focus:ring-2 focus:ring-sky-400 focus:ring-offset-2 focus:ring-offset-slate-950"
        >
          Cerrar sesión
        </button>
      </header>
    </main>
  )
}
