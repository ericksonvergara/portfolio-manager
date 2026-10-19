/** Shown while the session is restored after a page load, so the login page does not flash. */
export function SessionRestoring() {
  return (
    <main className="flex min-h-screen items-center justify-center bg-slate-950">
      <p role="status" className="text-sm text-slate-400">
        Restaurando sesión...
      </p>
    </main>
  )
}
