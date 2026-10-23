import { isAxiosError } from 'axios'
import { useState } from 'react'
import { ConfirmDialog } from '../../features/admin/ConfirmDialog'
import {
  ErrorState,
  LoadingState,
} from '../../features/admin/projects/AdminStates'
import { useAdminData } from '../../features/admin/projects/useAdminData'
import {
  createTechnology,
  deleteTechnology,
  listAdminTechnologies,
  updateTechnology,
  type AdminTechnology,
  type TechnologyInput,
} from '../../features/admin/technologies/adminTechnologiesApi'
import { TechnologyForm } from '../../features/admin/technologies/TechnologyForm'
import { CATEGORY_LABELS } from '../../features/landing/labels'

function byDisplayOrder(a: AdminTechnology, b: AdminTechnology) {
  return a.displayOrder - b.displayOrder || a.name.localeCompare(b.name, 'es')
}

/** The technology catalog: create, edit, list and delete (HU021). */
export function TechnologiesPage() {
  const content = useAdminData(listAdminTechnologies)
  const [changed, setChanged] = useState<AdminTechnology[] | null>(null)
  const [editing, setEditing] = useState<AdminTechnology | null>(null)
  const [pending, setPending] = useState<AdminTechnology | null>(null)
  const [busy, setBusy] = useState(false)
  const [deleteError, setDeleteError] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)

  const technologies =
    changed ?? (content.status === 'ready' ? content.data : [])

  const save = async (input: TechnologyInput) => {
    if (editing) {
      const updated = await updateTechnology(editing.id, input)
      setChanged(technologies.map((t) => (t.id === updated.id ? updated : t)))
      setNotice(`Tecnología «${updated.name}» actualizada.`)
      setEditing(null)
    } else {
      const created = await createTechnology(input)
      setChanged([...technologies, created])
      setNotice(`Tecnología «${created.name}» agregada.`)
    }
  }

  const confirmDelete = async () => {
    if (!pending) return
    setBusy(true)
    setDeleteError(null)
    try {
      await deleteTechnology(pending.id)
    } catch (error) {
      const status = isAxiosError(error) ? error.response?.status : undefined
      if (status !== 404) {
        setDeleteError(
          status === 409
            ? 'La usa al menos un proyecto: quítala de esos proyectos antes de eliminarla.'
            : 'No se pudo eliminar la tecnología. Inténtalo de nuevo.',
        )
        setBusy(false)
        return
      }
    }
    setChanged(technologies.filter((t) => t.id !== pending.id))
    setNotice(`Tecnología «${pending.name}» eliminada.`)
    if (editing?.id === pending.id) setEditing(null)
    setPending(null)
    setBusy(false)
  }

  return (
    <section aria-labelledby="technologies-title" className="max-w-4xl">
      <h1 id="technologies-title" className="mb-2 text-2xl font-bold">
        Tecnologías
      </h1>
      <p className="mb-6 text-sm text-slate-400">
        El catálogo que se muestra en la landing y que eligen los proyectos.
      </p>

      {notice && (
        <p
          role="status"
          className="mb-4 rounded-lg border border-emerald-800 bg-emerald-950 px-3 py-2 text-sm text-emerald-200"
        >
          {notice}
        </p>
      )}

      {content.status === 'loading' && (
        <LoadingState label="Cargando tecnologías…" />
      )}
      {content.status === 'error' && (
        <ErrorState
          message="No se pudo cargar el catálogo."
          retry={content.retry}
        />
      )}
      {content.status === 'ready' && (
        <>
          <TechnologyForm
            editing={editing}
            onSave={save}
            onCancel={() => setEditing(null)}
          />
          {technologies.length === 0 ? (
            <p className="rounded-lg border border-dashed border-slate-700 px-4 py-8 text-center text-slate-400">
              El catálogo está vacío. Agrega la primera tecnología.
            </p>
          ) : (
            <div className="overflow-x-auto rounded-lg border border-slate-800">
              <table className="w-full min-w-[560px] text-left text-sm">
                <caption className="sr-only">Catálogo de tecnologías</caption>
                <thead className="bg-slate-900 text-slate-400">
                  <tr>
                    <th scope="col" className="px-4 py-3 font-medium">
                      Nombre
                    </th>
                    <th scope="col" className="px-4 py-3 font-medium">
                      Categoría
                    </th>
                    <th scope="col" className="px-4 py-3 font-medium">
                      Orden
                    </th>
                    <th scope="col" className="px-4 py-3 font-medium">
                      Proyectos
                    </th>
                    <th scope="col" className="px-4 py-3 font-medium">
                      <span className="sr-only">Acciones</span>
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800">
                  {[...technologies].sort(byDisplayOrder).map((technology) => {
                    const inUse = technology.projectCount > 0
                    return (
                      <tr key={technology.id}>
                        <th
                          scope="row"
                          className="px-4 py-3 font-medium text-white"
                        >
                          {technology.name}
                        </th>
                        <td className="px-4 py-3 text-slate-300">
                          {CATEGORY_LABELS[technology.category]}
                        </td>
                        <td className="px-4 py-3 text-slate-300">
                          {technology.displayOrder}
                        </td>
                        <td className="px-4 py-3 text-slate-300">
                          {technology.projectCount}
                        </td>
                        <td className="whitespace-nowrap px-4 py-3 text-right">
                          <button
                            type="button"
                            onClick={() => setEditing(technology)}
                            aria-label={`Editar ${technology.name}`}
                            className="rounded-lg border border-slate-700 px-3 py-1 text-xs font-semibold text-slate-200 hover:bg-slate-900 focus:outline-none focus:ring-2 focus:ring-sky-400"
                          >
                            Editar
                          </button>
                          <button
                            type="button"
                            onClick={() => setPending(technology)}
                            disabled={inUse}
                            title={
                              inUse
                                ? `La usan ${technology.projectCount} proyecto(s)`
                                : undefined
                            }
                            aria-label={
                              inUse
                                ? `No se puede eliminar ${technology.name}: la usan ${technology.projectCount} proyecto(s)`
                                : `Eliminar ${technology.name}`
                            }
                            className="ml-2 rounded-lg border border-red-900 px-3 py-1 text-xs font-semibold text-red-300 hover:bg-red-950 focus:outline-none focus:ring-2 focus:ring-red-400 disabled:cursor-not-allowed disabled:opacity-40"
                          >
                            Eliminar
                          </button>
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
          )}
        </>
      )}

      {pending && (
        <ConfirmDialog
          title="Eliminar tecnología"
          message={`¿Eliminar «${pending.name}» del catálogo? Esta acción no se puede deshacer.`}
          confirmLabel="Eliminar"
          busy={busy}
          error={deleteError}
          onConfirm={() => void confirmDelete()}
          onCancel={() => {
            setPending(null)
            setDeleteError(null)
          }}
        />
      )}
    </section>
  )
}
