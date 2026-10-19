import { useEffect, type ReactNode } from 'react'
import { Link, useParams } from 'react-router-dom'
import type { ProjectDetail } from '../features/landing/landingApi'
import { STATUS_LABELS } from '../features/landing/labels'
import {
  formatProjectDate,
  toVideoEmbedUrl,
} from '../features/landing/projectFormat'
import { useProjectDetail } from '../features/landing/useProjectDetail'

function PageFrame({ children }: { children: ReactNode }) {
  return (
    <div className="min-h-screen bg-slate-950 text-slate-200">
      <header className="border-b border-slate-800/80">
        <div className="mx-auto max-w-5xl px-4 py-4 sm:px-6">
          <Link
            to="/"
            className="text-sm font-semibold text-sky-400 hover:text-sky-300 focus:outline-none focus-visible:ring-2 focus-visible:ring-sky-400"
          >
            ← Volver al portafolio
          </Link>
        </div>
      </header>
      <main className="mx-auto max-w-5xl px-4 py-10 sm:px-6 sm:py-14">
        {children}
      </main>
    </div>
  )
}

function DetailSection({
  id,
  title,
  children,
}: {
  id: string
  title: string
  children: ReactNode
}) {
  return (
    <section aria-labelledby={id} className="mt-12">
      <h2 id={id} className="mb-4 text-xl font-bold text-white">
        {title}
      </h2>
      {children}
    </section>
  )
}

function ProjectVideo({ name, videoUrl }: { name: string; videoUrl: string }) {
  const embedUrl = toVideoEmbedUrl(videoUrl)
  if (!embedUrl) {
    return (
      <a
        href={videoUrl}
        target="_blank"
        rel="noopener noreferrer"
        className="font-semibold text-sky-400 hover:text-sky-300 focus:outline-none focus-visible:ring-2 focus-visible:ring-sky-400"
      >
        Ver el video demostrativo
      </a>
    )
  }
  return (
    <div className="aspect-video w-full overflow-hidden rounded-2xl border border-slate-800">
      <iframe
        src={embedUrl}
        title={`Video demostrativo de ${name}`}
        className="h-full w-full"
        loading="lazy"
        allow="accelerometer; encrypted-media; gyroscope; picture-in-picture"
        allowFullScreen
      />
    </div>
  )
}

function ProjectView({ project }: { project: ProjectDetail }) {
  return (
    <article>
      <dl className="flex flex-wrap gap-x-6 gap-y-2 text-sm">
        <div className="flex gap-2">
          <dt className="text-slate-400">Estado:</dt>
          <dd className="font-semibold text-sky-300">
            {STATUS_LABELS[project.status]}
          </dd>
        </div>
        {project.developedOn && (
          <div className="flex gap-2">
            <dt className="text-slate-400">Fecha:</dt>
            <dd className="text-slate-200">
              {formatProjectDate(project.developedOn)}
            </dd>
          </div>
        )}
      </dl>

      <h1 className="mt-3 text-3xl font-bold tracking-tight text-balance text-white sm:text-4xl">
        {project.name}
      </h1>

      {project.technologies.length > 0 && (
        <ul
          className="mt-5 flex flex-wrap gap-2"
          aria-label={`Tecnologías de ${project.name}`}
        >
          {project.technologies.map((technology) => (
            <li
              key={technology}
              className="rounded-full border border-slate-700 bg-slate-900 px-3 py-1 text-sm text-slate-200"
            >
              {technology}
            </li>
          ))}
        </ul>
      )}

      {(project.projectUrl || project.repositoryUrl) && (
        <div className="mt-6 flex flex-wrap gap-3">
          {project.projectUrl && (
            <a
              href={project.projectUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="rounded-lg bg-sky-600 px-5 py-2.5 font-semibold text-white transition hover:bg-sky-500 focus:outline-none focus-visible:ring-2 focus-visible:ring-sky-300 focus-visible:ring-offset-2 focus-visible:ring-offset-slate-950"
            >
              Ver proyecto en línea
            </a>
          )}
          {project.repositoryUrl && (
            <a
              href={project.repositoryUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="rounded-lg border border-slate-700 px-5 py-2.5 font-semibold text-slate-200 transition hover:border-slate-500 focus:outline-none focus-visible:ring-2 focus-visible:ring-sky-400"
            >
              Repositorio
            </a>
          )}
        </div>
      )}

      <img
        src={project.coverImageUrl}
        alt={`Imagen principal de ${project.name}`}
        width={1280}
        height={720}
        className="mt-8 aspect-video w-full rounded-2xl border border-slate-800 object-cover"
      />

      <DetailSection id="descripcion" title="Descripción">
        <p className="max-w-3xl text-lg leading-relaxed whitespace-pre-line text-slate-300">
          {project.description}
        </p>
      </DetailSection>

      {project.gallery.length > 0 && (
        <DetailSection id="galeria" title="Galería">
          <ul className="grid gap-4 sm:grid-cols-2">
            {project.gallery.map((image, index) => (
              <li key={`${image.url}-${index}`}>
                <img
                  src={image.url}
                  alt={
                    image.altText ??
                    `Imagen ${index + 1} de la galería de ${project.name}`
                  }
                  width={640}
                  height={360}
                  loading="lazy"
                  className="aspect-video w-full rounded-xl border border-slate-800 object-cover"
                />
              </li>
            ))}
          </ul>
        </DetailSection>
      )}

      {project.videoUrl && (
        <DetailSection id="video" title="Video">
          <ProjectVideo name={project.name} videoUrl={project.videoUrl} />
        </DetailSection>
      )}
    </article>
  )
}

/** Public detail of a published project (HU003). */
export function ProjectDetailPage() {
  const { slug = '' } = useParams()
  const content = useProjectDetail(slug)

  // Opening a project from the bottom of the landing page must not land mid-page.
  useEffect(() => {
    window.scrollTo(0, 0)
  }, [slug])

  if (content.status === 'loading') {
    return (
      <PageFrame>
        <p role="status" className="text-sm text-slate-400">
          Cargando proyecto...
        </p>
      </PageFrame>
    )
  }

  if (content.status === 'not-found') {
    return (
      <PageFrame>
        <h1 className="text-3xl font-bold text-white">
          Proyecto no encontrado
        </h1>
        <p className="mt-3 text-slate-400">
          El proyecto que buscas no existe o ya no está publicado.
        </p>
      </PageFrame>
    )
  }

  if (content.status === 'error') {
    return (
      <PageFrame>
        <p role="alert" className="text-slate-300">
          No se pudo cargar el proyecto. Revisa tu conexión e inténtalo de
          nuevo.
        </p>
        <button
          type="button"
          onClick={content.retry}
          className="mt-4 rounded-lg bg-sky-600 px-4 py-2 font-semibold text-white transition hover:bg-sky-500 focus:outline-none focus-visible:ring-2 focus-visible:ring-sky-300"
        >
          Reintentar
        </button>
      </PageFrame>
    )
  }

  return (
    <PageFrame>
      <ProjectView project={content.project} />
    </PageFrame>
  )
}
