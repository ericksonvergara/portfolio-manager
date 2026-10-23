import type { ReactNode } from 'react'

/** The centered card shared by the login and password recovery pages. */
export function AuthCard({
  titleId,
  title,
  subtitle,
  children,
}: {
  titleId: string
  title: string
  subtitle: string
  children: ReactNode
}) {
  return (
    <main className="flex min-h-screen items-center justify-center bg-slate-950 px-4">
      <section
        aria-labelledby={titleId}
        className="w-full max-w-sm rounded-2xl border border-slate-800 bg-slate-900 p-8 shadow-xl"
      >
        <h1 id={titleId} className="mb-1 text-2xl font-bold text-white">
          {title}
        </h1>
        <p className="mb-6 text-sm text-slate-400">{subtitle}</p>
        {children}
      </section>
    </main>
  )
}

export const AUTH_INPUT =
  'w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-white outline-none focus:border-sky-500 focus:ring-2 focus:ring-sky-500/40 aria-[invalid=true]:border-red-500'

export const AUTH_BUTTON =
  'w-full rounded-lg bg-sky-600 px-4 py-2 font-semibold text-white transition hover:bg-sky-500 focus:outline-none focus:ring-2 focus:ring-sky-400 focus:ring-offset-2 focus:ring-offset-slate-900 disabled:cursor-not-allowed disabled:opacity-60'

export const AUTH_LINK =
  'text-sm text-sky-400 underline-offset-4 hover:underline focus:outline-none focus:ring-2 focus:ring-sky-400'
