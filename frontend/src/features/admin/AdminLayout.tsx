import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'

const NAV_ITEMS = [
  { to: '/admin/proyectos', label: 'Proyectos' },
  { to: '/admin/cuenta', label: 'Cuenta' },
]

/**
 * Frame of the admin panel: header, section navigation and the current page.
 */
export function AdminLayout() {
  const { session, logout } = useAuth()

  return (
    <div className="min-h-screen bg-slate-950 text-white">
      <a
        href="#admin-content"
        className="sr-only focus:not-sr-only focus:absolute focus:left-4 focus:top-4 focus:rounded-lg focus:bg-sky-600 focus:px-4 focus:py-2"
      >
        Saltar al contenido
      </a>
      <header className="flex flex-wrap items-center justify-between gap-4 border-b border-slate-800 px-6 py-4">
        <div>
          <p className="text-lg font-bold">Panel administrativo</p>
          <p className="text-sm text-slate-400">
            {session?.user.name || session?.user.email}
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

      <div className="flex flex-col md:flex-row">
        <nav
          aria-label="Secciones del panel"
          className="border-b border-slate-800 px-4 py-3 md:min-h-[calc(100vh-81px)] md:w-56 md:border-b-0 md:border-r md:py-6"
        >
          <ul className="flex gap-2 md:flex-col">
            {NAV_ITEMS.map((item) => (
              <li key={item.to}>
                <NavLink
                  to={item.to}
                  className={({ isActive }) =>
                    `block rounded-lg px-3 py-2 text-sm font-medium transition focus:outline-none focus:ring-2 focus:ring-sky-400 ${
                      isActive
                        ? 'bg-sky-600 text-white'
                        : 'text-slate-300 hover:bg-slate-900'
                    }`
                  }
                >
                  {item.label}
                </NavLink>
              </li>
            ))}
          </ul>
        </nav>

        <main id="admin-content" className="flex-1 px-4 py-6 md:px-8">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
