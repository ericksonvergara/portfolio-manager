import { useCallback, useState } from 'react'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import {
  getAdminProject,
  updateProject,
  type AdminProject,
  type ProjectInput,
} from '../../features/admin/projects/adminProjectsApi'
import {
  ErrorState,
  LoadingState,
} from '../../features/admin/projects/AdminStates'
import { ProjectForm } from '../../features/admin/projects/ProjectForm'
import { ProjectImages } from '../../features/admin/projects/ProjectImages'
import {
  fromAdminProject,
  toProjectFormError,
} from '../../features/admin/projects/projectFormModel'
import { useAdminData } from '../../features/admin/projects/useAdminData'
import { getTechnologies } from '../../features/landing/landingApi'

const LIST = '/admin/proyectos'

/** Edits the data (HU016) and the images (HU019) of an existing project. */
export function EditProjectPage() {
  const navigate = useNavigate()
  const created = (useLocation().state as { created?: string } | null)?.created
  const [images, setImages] = useState<AdminProject | null>(null)
  const id = Number(useParams().id)
  const load = useCallback(
    () =>
      Number.isInteger(id) && id > 0
        ? Promise.all([getAdminProject(id), getTechnologies()])
        : Promise.reject(new Error('invalid id')),
    [id],
  )
  const content = useAdminData(load)

  const save = async (input: ProjectInput) => {
    try {
      const project = await updateProject(id, input)
      navigate(LIST, { state: { updated: project.name } })
    } catch (error) {
      throw toProjectFormError(error)
    }
  }

  return (
    <section aria-labelledby="edit-project-title">
      <h1 id="edit-project-title" className="mb-6 text-2xl font-bold">
        Editar proyecto
      </h1>
      {content.status === 'loading' && (
        <LoadingState label="Cargando proyecto…" />
      )}
      {content.status === 'not-found' && (
        <div className="rounded-lg border border-slate-800 px-4 py-6 text-slate-300">
          <p>Proyecto no encontrado. Puede que se haya eliminado.</p>
          <Link
            to={LIST}
            className="mt-2 inline-block text-sky-400 underline-offset-4 hover:underline focus:outline-none focus:ring-2 focus:ring-sky-400"
          >
            ← Volver a proyectos
          </Link>
        </div>
      )}
      {content.status === 'error' && (
        <ErrorState
          message="No se pudo cargar el proyecto."
          retry={content.retry}
        />
      )}
      {content.status === 'ready' && (
        <>
          {created && (
            <p
              role="status"
              className="mb-4 rounded-lg border border-emerald-800 bg-emerald-950 px-3 py-2 text-sm text-emerald-200"
            >
              Proyecto «{created}» registrado. Agrega su imagen principal para
              poder publicarlo.
            </p>
          )}
          <p className="mb-6 text-sm text-slate-400">
            URL pública:{' '}
            <code className="text-slate-200">
              /proyectos/{content.data[0].slug}
            </code>
            . Si cambias el nombre, la URL cambia con él.
          </p>
          <ProjectForm
            technologies={content.data[1]}
            defaultValues={fromAdminProject(content.data[0])}
            submitLabel="Guardar cambios"
            onSubmit={save}
            onCancel={() => navigate(LIST)}
          />
          <ProjectImages
            project={images ?? content.data[0]}
            onChange={setImages}
          />
        </>
      )}
    </section>
  )
}
