import { useForm, useWatch } from 'react-hook-form'
import type {
  ProjectStatus,
  Technology,
  TechnologyCategory,
} from '../../landing/landingApi'
import { CATEGORY_LABELS, STATUS_LABELS } from '../../landing/labels'
import { WEB_URL_MESSAGE } from '../../../services/validation'
import type { ProjectInput } from './adminProjectsApi'
import {
  EMPTY_PROJECT,
  isWebUrl,
  ProjectFormError,
  type ProjectFormValues,
  toProjectInput,
} from './projectFormModel'

const INPUT_CLASS =
  'w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-white outline-none focus:border-sky-500 focus:ring-2 focus:ring-sky-500/40 aria-[invalid=true]:border-red-500'

interface ProjectFormProps {
  technologies: Technology[]
  defaultValues?: ProjectFormValues
  submitLabel: string
  onSubmit: (input: ProjectInput) => Promise<void>
  onCancel: () => void
}

export function ProjectForm({
  technologies,
  defaultValues = EMPTY_PROJECT,
  submitLabel,
  onSubmit,
  onCancel,
}: ProjectFormProps) {
  const {
    register,
    handleSubmit,
    control,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<ProjectFormValues>({ mode: 'onTouched', defaultValues })

  const today = new Date().toLocaleDateString('en-CA')
  const shortDescription = useWatch({ control, name: 'shortDescription' })

  const submit = async (values: ProjectFormValues) => {
    try {
      await onSubmit(toProjectInput(values))
    } catch (error) {
      const message =
        error instanceof ProjectFormError
          ? error.message
          : 'No se pudo guardar el proyecto. Inténtalo de nuevo.'
      const field = error instanceof ProjectFormError ? error.field : undefined
      setError(field ?? 'root', { message })
    }
  }

  const byCategory = Object.keys(CATEGORY_LABELS)
    .map((category) => ({
      category: category as TechnologyCategory,
      items: technologies.filter((t) => t.category === category),
    }))
    .filter((group) => group.items.length > 0)

  const fieldError = (name: keyof ProjectFormValues) =>
    errors[name] ? (
      <p id={`${name}-error`} className="mt-1 text-sm text-red-400">
        {errors[name]?.message}
      </p>
    ) : null

  const a11y = (name: keyof ProjectFormValues) => ({
    'aria-invalid': errors[name] ? ('true' as const) : ('false' as const),
    'aria-describedby': errors[name] ? `${name}-error` : undefined,
  })

  const urlRules = {
    validate: (value: string) =>
      !value.trim() || isWebUrl(value.trim()) || WEB_URL_MESSAGE,
    maxLength: { value: 500, message: 'Máximo 500 caracteres.' },
  }

  return (
    <form
      noValidate
      onSubmit={handleSubmit(submit)}
      className="max-w-3xl space-y-6"
    >
      {errors.root && (
        <p
          role="alert"
          className="rounded-lg border border-red-800 bg-red-950 px-3 py-2 text-sm text-red-300"
        >
          {errors.root.message}
        </p>
      )}

      <div>
        <label
          htmlFor="name"
          className="mb-1 block text-sm font-medium text-slate-200"
        >
          Nombre
        </label>
        <input
          id="name"
          className={INPUT_CLASS}
          {...a11y('name')}
          {...register('name', {
            validate: (value) =>
              value.trim() !== '' || 'El nombre es obligatorio.',
            maxLength: { value: 100, message: 'Máximo 100 caracteres.' },
          })}
        />
        {fieldError('name')}
      </div>

      <div>
        <label
          htmlFor="shortDescription"
          className="mb-1 block text-sm font-medium text-slate-200"
        >
          Descripción corta
        </label>
        <textarea
          id="shortDescription"
          rows={2}
          className={INPUT_CLASS}
          {...a11y('shortDescription')}
          {...register('shortDescription', {
            validate: (value) =>
              value.trim() !== '' || 'La descripción corta es obligatoria.',
            maxLength: { value: 300, message: 'Máximo 300 caracteres.' },
          })}
        />
        <p className="mt-1 text-xs text-slate-500">
          Se muestra en la tarjeta del portafolio · {shortDescription.length}
          /300
        </p>
        {fieldError('shortDescription')}
      </div>

      <div>
        <label
          htmlFor="description"
          className="mb-1 block text-sm font-medium text-slate-200"
        >
          Descripción completa
        </label>
        <textarea
          id="description"
          rows={6}
          className={INPUT_CLASS}
          {...a11y('description')}
          {...register('description', {
            validate: (value) =>
              value.trim() !== '' || 'La descripción completa es obligatoria.',
            maxLength: { value: 5000, message: 'Máximo 5000 caracteres.' },
          })}
        />
        {fieldError('description')}
      </div>

      <div className="grid gap-6 sm:grid-cols-2">
        <div>
          <label
            htmlFor="projectUrl"
            className="mb-1 block text-sm font-medium text-slate-200"
          >
            URL del proyecto <span className="text-slate-500">(opcional)</span>
          </label>
          <input
            id="projectUrl"
            type="url"
            placeholder="https://"
            className={INPUT_CLASS}
            {...a11y('projectUrl')}
            {...register('projectUrl', urlRules)}
          />
          {fieldError('projectUrl')}
        </div>
        <div>
          <label
            htmlFor="repositoryUrl"
            className="mb-1 block text-sm font-medium text-slate-200"
          >
            Repositorio <span className="text-slate-500">(opcional)</span>
          </label>
          <input
            id="repositoryUrl"
            type="url"
            placeholder="https://github.com/…"
            className={INPUT_CLASS}
            {...a11y('repositoryUrl')}
            {...register('repositoryUrl', urlRules)}
          />
          {fieldError('repositoryUrl')}
        </div>
        <div className="sm:col-span-2">
          <label
            htmlFor="videoUrl"
            className="mb-1 block text-sm font-medium text-slate-200"
          >
            Video{' '}
            <span className="text-slate-500">
              (opcional, por ejemplo de YouTube)
            </span>
          </label>
          <input
            id="videoUrl"
            type="url"
            placeholder="https://www.youtube.com/watch?v=…"
            className={INPUT_CLASS}
            {...a11y('videoUrl')}
            {...register('videoUrl', urlRules)}
          />
          {fieldError('videoUrl')}
        </div>
      </div>

      <div className="grid gap-6 sm:grid-cols-3">
        <div>
          <label
            htmlFor="status"
            className="mb-1 block text-sm font-medium text-slate-200"
          >
            Estado
          </label>
          <select id="status" className={INPUT_CLASS} {...register('status')}>
            {(Object.keys(STATUS_LABELS) as ProjectStatus[]).map((status) => (
              <option key={status} value={status}>
                {STATUS_LABELS[status]}
              </option>
            ))}
          </select>
        </div>
        <div>
          <label
            htmlFor="developedOn"
            className="mb-1 block text-sm font-medium text-slate-200"
          >
            Fecha <span className="text-slate-500">(opcional)</span>
          </label>
          <input
            id="developedOn"
            type="date"
            max={today}
            className={INPUT_CLASS}
            {...a11y('developedOn')}
            {...register('developedOn', {
              validate: (value) =>
                !value || value <= today || 'La fecha no puede ser futura.',
            })}
          />
          {fieldError('developedOn')}
        </div>
        <div>
          <label
            htmlFor="displayOrder"
            className="mb-1 block text-sm font-medium text-slate-200"
          >
            Orden de aparición
          </label>
          <input
            id="displayOrder"
            type="number"
            min={0}
            step={1}
            className={INPUT_CLASS}
            {...a11y('displayOrder')}
            {...register('displayOrder', {
              required: 'El orden es obligatorio.',
              validate: (value) =>
                (Number.isInteger(Number(value)) && Number(value) >= 0) ||
                'Usa un número entero mayor o igual a 0.',
            })}
          />
          {fieldError('displayOrder')}
        </div>
      </div>

      <label className="flex items-center gap-3 text-sm text-slate-200">
        <input
          type="checkbox"
          className="h-4 w-4 rounded border-slate-600 bg-slate-950 accent-sky-500"
          {...register('featured')}
        />
        Destacado (aparece primero en el portafolio)
      </label>

      <fieldset
        aria-describedby={
          errors.technologyIds ? 'technologyIds-error' : undefined
        }
      >
        <legend className="mb-2 text-sm font-medium text-slate-200">
          Tecnologías
        </legend>
        {byCategory.length === 0 && (
          <p className="text-sm text-slate-400">
            El catálogo de tecnologías está vacío.
          </p>
        )}
        <div className="grid gap-4 sm:grid-cols-2">
          {byCategory.map((group) => (
            <div key={group.category}>
              <p className="mb-1 text-xs font-semibold uppercase tracking-wide text-slate-500">
                {CATEGORY_LABELS[group.category]}
              </p>
              <ul className="space-y-1">
                {group.items.map((technology) => (
                  <li key={technology.id}>
                    <label className="flex items-center gap-2 text-sm text-slate-200">
                      <input
                        type="checkbox"
                        value={String(technology.id)}
                        className="h-4 w-4 accent-sky-500"
                        {...register('technologyIds', {
                          validate: (value) =>
                            value.length > 0 ||
                            'Selecciona al menos una tecnología.',
                        })}
                      />
                      {technology.name}
                    </label>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>
        {fieldError('technologyIds')}
      </fieldset>

      <p className="rounded-lg border border-slate-800 bg-slate-900 px-3 py-2 text-sm text-slate-400">
        Las imágenes se agregan después de guardar. El proyecto queda inactivo
        hasta que tenga imagen principal.
      </p>

      <div className="flex flex-wrap gap-3">
        <button
          type="submit"
          disabled={isSubmitting}
          className="rounded-lg bg-sky-600 px-4 py-2 font-semibold text-white transition hover:bg-sky-500 focus:outline-none focus:ring-2 focus:ring-sky-400 focus:ring-offset-2 focus:ring-offset-slate-950 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {isSubmitting ? 'Guardando…' : submitLabel}
        </button>
        <button
          type="button"
          onClick={onCancel}
          className="rounded-lg border border-slate-700 px-4 py-2 font-semibold text-slate-200 transition hover:bg-slate-900 focus:outline-none focus:ring-2 focus:ring-sky-400"
        >
          Cancelar
        </button>
      </div>
    </form>
  )
}
