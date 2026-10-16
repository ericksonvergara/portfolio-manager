import { useAuth } from '../features/auth/useAuth'

/**
 * Placeholder of the admin panel; the dashboard arrives in later sprints.
 */
export function AdminPage() {
  const { session } = useAuth()

  return (
    <main className="min-h-screen bg-slate-950 px-6 py-10 text-white">
      <h1 className="text-3xl font-bold">Panel administrativo</h1>
      <p className="mt-2 text-slate-400">
        Bienvenido, {session?.user.name || session?.user.email}.
      </p>
    </main>
  )
}
