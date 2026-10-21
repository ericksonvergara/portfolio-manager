import { isAxiosError } from 'axios'
import { useNavigate } from 'react-router-dom'
import {
  createProject,
  type ProjectInput,
} from '../../features/admin/projects/adminProjectsApi'
import {
  ErrorState,
  LoadingState,
} from '../../features/admin/projects/AdminStates'
import { ProjectForm } from '../../features/admin/projects/ProjectForm'
import { ProjectFormError } from '../../features/admin/projects/projectFormModel'
import { useAdminData } from '../../features/admin/projects/useAdminData'
import { getTechnologies } from '../../features/landing/landingApi'

const LIST = '/admin/proyectos'

function toFormError(error: unknown): ProjectFormError {
  if (isAxiosError(error) && error.response?.status === 409) {
    return new ProjectFormError('Ya existe un proyecto con ese nombre.', 'name')
  }
  if (isAxiosError(error) && error.response?.status === 400) {
    return new ProjectFormError('Revisa los datos ingresados.')
  }
  return new ProjectFormError(
    'No se pudo guardar el proyecto. Inténtalo de nuevo.',
  )
}

/** Registers a new project (HU015). */
export function NewProjectPage() {
  const navigate = useNavigate()
  const technologies = useAdminData(getTechnologies)

  const save = async (input: ProjectInput) => {
    try {
      const project = await createProject(input)
      navigate(LIST, { state: { created: project.name } })
    } catch (error) {
      throw toFormError(error)
    }
  }

  return (
    <section aria-labelledby="new-project-title">
      <h1 id="new-project-title" className="mb-6 text-2xl font-bold">
        Registrar proyecto
      </h1>
      {technologies.status === 'loading' && (
        <LoadingState label="Cargando tecnologías…" />
      )}
      {technologies.status === 'error' && (
        <ErrorState
          message="No se pudo cargar el catálogo de tecnologías."
          retry={technologies.retry}
        />
      )}
      {technologies.status === 'ready' && (
        <ProjectForm
          technologies={technologies.data}
          submitLabel="Registrar proyecto"
          onSubmit={save}
          onCancel={() => navigate(LIST)}
        />
      )}
    </section>
  )
}
