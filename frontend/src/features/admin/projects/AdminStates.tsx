export function LoadingState({ label }: { label: string }) {
  return (
    <p role="status" className="text-slate-400">
      {label}
    </p>
  )
}

export function ErrorState({
  message,
  retry,
}: {
  message: string
  retry: () => void
}) {
  return (
    <div
      role="alert"
      className="rounded-lg border border-red-800 bg-red-950 px-4 py-3 text-red-300"
    >
      <p>{message}</p>
      <button
        type="button"
        onClick={retry}
        className="mt-2 rounded-lg border border-red-700 px-3 py-1 text-sm font-semibold text-red-200 hover:bg-red-900 focus:outline-none focus:ring-2 focus:ring-red-400"
      >
        Reintentar
      </button>
    </div>
  )
}
