import { isAxiosError } from 'axios'
import { useForm } from 'react-hook-form'
import type { TechnologyCategory } from '../../landing/landingApi'
import { CATEGORY_LABELS } from '../../landing/labels'
import type { AdminTechnology, TechnologyInput } from './adminTechnologiesApi'

interface TechnologyValues {
  name: string
  category: TechnologyCategory
  displayOrder: string
}

const INPUT =
  'w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-white outline-none focus:border-sky-500 focus:ring-2 focus:ring-sky-500/40 aria-[invalid=true]:border-red-500'

interface TechnologyFormProps {
  /** The technology being edited, or null to create a new one. */
  editing: AdminTechnology | null
  onSave: (input: TechnologyInput) => Promise<void>
  onCancel: () => void
}

/** Creates or edits a technology of the catalog (HU021). */
export function TechnologyForm({
  editing,
  onSave,
  onCancel,
}: TechnologyFormProps) {
  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<TechnologyValues>({
    mode: 'onTouched',
    values: editing
      ? {
          name: editing.name,
          category: editing.category,
          displayOrder: String(editing.displayOrder),
        }
      : { name: '', category: 'BACKEND', displayOrder: '0' },
  })

  const submit = async (values: TechnologyValues) => {
    try {
      await onSave({
        name: values.name.trim(),
        category: values.category,
        displayOrder: Number(values.displayOrder),
      })
      if (!editing) reset()
    } catch (error) {
      if (isAxiosError(error) && error.response?.status === 409) {
        setError('name', {
          message: 'Ya existe una tecnología con ese nombre.',
        })
      } else {
        setError('root', {
          message: 'No se pudo guardar la tecnología. Inténtalo de nuevo.',
        })
      }
    }
  }

  return (
    <form
      noValidate
      onSubmit={handleSubmit(submit)}
      aria-labelledby="technology-form-title"
      className="mb-8 rounded-lg border border-slate-800 p-4"
    >
      <h2 id="technology-form-title" className="mb-4 font-semibold">
        {editing ? `Editar «${editing.name}»` : 'Nueva tecnología'}
      </h2>
      {errors.root && (
        <p
          role="alert"
          className="mb-4 rounded-lg border border-red-800 bg-red-950 px-3 py-2 text-sm text-red-300"
        >
          {errors.root.message}
        </p>
      )}
      <div className="grid gap-4 sm:grid-cols-[2fr_1.5fr_1fr]">
        <div>
          <label
            htmlFor="technology-name"
            className="mb-1 block text-sm font-medium text-slate-200"
          >
            Nombre
          </label>
          <input
            id="technology-name"
            className={INPUT}
            aria-invalid={errors.name ? 'true' : 'false'}
            aria-describedby={errors.name ? 'technology-name-error' : undefined}
            {...register('name', {
              validate: (value) =>
                value.trim() !== '' || 'El nombre es obligatorio.',
              maxLength: { value: 50, message: 'Máximo 50 caracteres.' },
            })}
          />
          {errors.name && (
            <p id="technology-name-error" className="mt-1 text-sm text-red-400">
              {errors.name.message}
            </p>
          )}
        </div>
        <div>
          <label
            htmlFor="technology-category"
            className="mb-1 block text-sm font-medium text-slate-200"
          >
            Categoría
          </label>
          <select
            id="technology-category"
            className={INPUT}
            {...register('category')}
          >
            {(Object.keys(CATEGORY_LABELS) as TechnologyCategory[]).map(
              (category) => (
                <option key={category} value={category}>
                  {CATEGORY_LABELS[category]}
                </option>
              ),
            )}
          </select>
        </div>
        <div>
          <label
            htmlFor="technology-order"
            className="mb-1 block text-sm font-medium text-slate-200"
          >
            Orden
          </label>
          <input
            id="technology-order"
            type="number"
            min={0}
            step={1}
            className={INPUT}
            aria-invalid={errors.displayOrder ? 'true' : 'false'}
            aria-describedby={
              errors.displayOrder ? 'technology-order-error' : undefined
            }
            {...register('displayOrder', {
              validate: (value) =>
                (value !== '' &&
                  Number.isInteger(Number(value)) &&
                  Number(value) >= 0) ||
                'Usa un número entero mayor o igual a 0.',
            })}
          />
          {errors.displayOrder && (
            <p
              id="technology-order-error"
              className="mt-1 text-sm text-red-400"
            >
              {errors.displayOrder.message}
            </p>
          )}
        </div>
      </div>
      <div className="mt-4 flex flex-wrap gap-3">
        <button
          type="submit"
          disabled={isSubmitting}
          className="rounded-lg bg-sky-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-sky-500 focus:outline-none focus:ring-2 focus:ring-sky-400 focus:ring-offset-2 focus:ring-offset-slate-950 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {isSubmitting
            ? 'Guardando…'
            : editing
              ? 'Guardar cambios'
              : 'Agregar tecnología'}
        </button>
        {editing && (
          <button
            type="button"
            onClick={onCancel}
            className="rounded-lg border border-slate-700 px-4 py-2 text-sm font-semibold text-slate-200 hover:bg-slate-900 focus:outline-none focus:ring-2 focus:ring-sky-400"
          >
            Cancelar edición
          </button>
        )}
      </div>
    </form>
  )
}
