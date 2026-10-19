import type { ReactNode } from 'react'
import { Link } from 'react-router-dom'
import { CATEGORY_LABELS, SOCIAL_LABELS } from './labels'
import type {
  Profile,
  ProjectCard,
  SocialLink,
  Technology,
  TechnologyCategory,
} from './landingApi'
import { SocialIcon } from './SocialIcon'

const SECTION_IDS = {
  about: 'sobre-mi',
  technologies: 'tecnologias',
  portfolio: 'portafolio',
  contact: 'contacto',
} as const

const NAV_ITEMS = [
  { id: SECTION_IDS.about, label: 'Sobre mí' },
  { id: SECTION_IDS.technologies, label: 'Tecnologías' },
  { id: SECTION_IDS.portfolio, label: 'Portafolio' },
  { id: SECTION_IDS.contact, label: 'Contacto' },
]

function Section({
  id,
  title,
  children,
}: {
  id: string
  title: string
  children: ReactNode
}) {
  return (
    <section
      id={id}
      aria-labelledby={`${id}-title`}
      className="scroll-mt-20 border-t border-slate-800/80 py-16 sm:py-20"
    >
      <div className="mx-auto max-w-5xl px-4 sm:px-6">
        <h2
          id={`${id}-title`}
          className="mb-8 text-2xl font-bold tracking-tight text-white sm:text-3xl"
        >
          {title}
        </h2>
        {children}
      </div>
    </section>
  )
}

export function LandingHeader({ brand }: { brand: string }) {
  return (
    <header className="sticky top-0 z-10 border-b border-slate-800/80 bg-slate-950/90 backdrop-blur">
      <a
        href="#contenido"
        className="sr-only focus:not-sr-only focus:absolute focus:left-4 focus:top-3 focus:rounded-md focus:bg-sky-600 focus:px-3 focus:py-2 focus:text-white"
      >
        Saltar al contenido
      </a>
      <div className="mx-auto flex max-w-5xl flex-wrap items-center justify-between gap-x-6 gap-y-2 px-4 py-3 sm:px-6">
        <a
          href="#inicio"
          className="font-semibold text-white focus:outline-none focus-visible:ring-2 focus-visible:ring-sky-400"
        >
          {brand}
        </a>
        <nav aria-label="Secciones">
          <ul className="flex flex-wrap gap-x-5 gap-y-1 text-sm text-slate-300">
            {NAV_ITEMS.map((item) => (
              <li key={item.id}>
                <a
                  href={`#${item.id}`}
                  className="rounded transition hover:text-white focus:outline-none focus-visible:ring-2 focus-visible:ring-sky-400"
                >
                  {item.label}
                </a>
              </li>
            ))}
          </ul>
        </nav>
      </div>
    </header>
  )
}

function SocialLinks({ links }: { links: SocialLink[] }) {
  if (links.length === 0) {
    return null
  }
  return (
    <ul className="flex flex-wrap gap-3" aria-label="Redes sociales">
      {links.map((link) => (
        <li key={link.platform}>
          <a
            href={link.url}
            target="_blank"
            rel="noopener noreferrer"
            aria-label={SOCIAL_LABELS[link.platform]}
            title={SOCIAL_LABELS[link.platform]}
            className="flex h-10 w-10 items-center justify-center rounded-full border border-slate-700 text-slate-300 transition hover:border-sky-500 hover:text-white focus:outline-none focus-visible:ring-2 focus-visible:ring-sky-400"
          >
            <SocialIcon platform={link.platform} />
          </a>
        </li>
      ))}
    </ul>
  )
}

function initials(fullName: string): string {
  return fullName
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((word) => word[0].toUpperCase())
    .join('')
}

export function HeroSection({ profile }: { profile: Profile | null }) {
  if (!profile) {
    return (
      <section id="inicio" className="mx-auto max-w-5xl px-4 py-24 sm:px-6">
        <h1 className="text-4xl font-bold tracking-tight text-white sm:text-5xl">
          Portafolio en construcción
        </h1>
        <p className="mt-4 max-w-xl text-lg text-slate-400">
          Muy pronto encontrarás aquí la presentación y los proyectos.
        </p>
      </section>
    )
  }

  return (
    <section
      id="inicio"
      className="mx-auto grid max-w-5xl items-center gap-10 px-4 py-20 sm:px-6 sm:py-28 md:grid-cols-[auto_1fr]"
    >
      {profile.photoUrl ? (
        <img
          src={profile.photoUrl}
          alt={`Fotografía de ${profile.fullName}`}
          width={176}
          height={176}
          className="h-36 w-36 rounded-full object-cover ring-4 ring-sky-500/30 sm:h-44 sm:w-44"
        />
      ) : (
        <div
          aria-hidden="true"
          className="flex h-36 w-36 items-center justify-center rounded-full bg-slate-800 text-4xl font-bold text-sky-300 ring-4 ring-sky-500/30 sm:h-44 sm:w-44"
        >
          {initials(profile.fullName)}
        </div>
      )}
      <div className="min-w-0">
        <p className="text-sm font-semibold uppercase tracking-widest text-sky-400">
          {profile.headline}
        </p>
        <h1 className="mt-2 text-4xl font-bold tracking-tight text-balance text-white sm:text-5xl">
          {profile.fullName}
        </h1>
        {profile.location && (
          <p className="mt-3 text-slate-400">{profile.location}</p>
        )}
        <div className="mt-6 flex flex-wrap items-center gap-4">
          <a
            href={`#${SECTION_IDS.portfolio}`}
            className="rounded-lg bg-sky-600 px-5 py-2.5 font-semibold text-white transition hover:bg-sky-500 focus:outline-none focus-visible:ring-2 focus-visible:ring-sky-300 focus-visible:ring-offset-2 focus-visible:ring-offset-slate-950"
          >
            Ver proyectos
          </a>
          <a
            href={`#${SECTION_IDS.contact}`}
            className="rounded-lg border border-slate-700 px-5 py-2.5 font-semibold text-slate-200 transition hover:border-slate-500 focus:outline-none focus-visible:ring-2 focus-visible:ring-sky-400"
          >
            Contactar
          </a>
          <SocialLinks links={profile.socialLinks} />
        </div>
      </div>
    </section>
  )
}

export function AboutSection({ profile }: { profile: Profile }) {
  return (
    <Section id={SECTION_IDS.about} title="Sobre mí">
      <p className="max-w-3xl text-lg leading-relaxed whitespace-pre-line text-slate-300">
        {profile.about}
      </p>
    </Section>
  )
}

export function TechnologiesSection({
  technologies,
}: {
  technologies: Technology[]
}) {
  const groups = (Object.keys(CATEGORY_LABELS) as TechnologyCategory[])
    .map((category) => ({
      category,
      items: technologies.filter((tech) => tech.category === category),
    }))
    .filter((group) => group.items.length > 0)

  return (
    <Section id={SECTION_IDS.technologies} title="Tecnologías">
      {groups.length === 0 ? (
        <p className="text-slate-400">
          Pronto verás aquí las tecnologías con las que trabajo.
        </p>
      ) : (
        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {groups.map((group) => (
            <div key={group.category}>
              <h3 className="mb-3 text-xs font-semibold uppercase tracking-widest text-slate-400">
                {CATEGORY_LABELS[group.category]}
              </h3>
              <ul className="flex flex-wrap gap-2">
                {group.items.map((tech) => (
                  <li
                    key={tech.name}
                    className="rounded-full border border-slate-700 bg-slate-900 px-3 py-1 text-sm text-slate-200"
                  >
                    {tech.name}
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>
      )}
    </Section>
  )
}

export function PortfolioSection({ projects }: { projects: ProjectCard[] }) {
  return (
    <Section id={SECTION_IDS.portfolio} title="Portafolio">
      {projects.length === 0 ? (
        <p className="text-slate-400">
          Los proyectos publicados aparecerán aquí.
        </p>
      ) : (
        <ul className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {projects.map((project) => (
            <ProjectCardItem key={project.slug} project={project} />
          ))}
        </ul>
      )}
    </Section>
  )
}

function ProjectCardItem({ project }: { project: ProjectCard }) {
  return (
    <li className="flex">
      <article className="flex flex-1 flex-col overflow-hidden rounded-2xl border border-slate-800 bg-slate-900">
        <img
          src={project.coverImageUrl}
          alt={`Imagen del proyecto ${project.name}`}
          width={640}
          height={360}
          loading="lazy"
          className="aspect-video w-full object-cover"
        />
        <div className="flex flex-1 flex-col gap-4 p-5">
          <div>
            <div className="flex items-start justify-between gap-3">
              <h3 className="text-lg font-semibold text-white">
                {project.name}
              </h3>
              {project.featured && (
                <span className="shrink-0 rounded-full bg-sky-500/15 px-2.5 py-0.5 text-xs font-semibold text-sky-300">
                  Destacado
                </span>
              )}
            </div>
            <p className="mt-2 text-sm leading-relaxed text-slate-400">
              {project.shortDescription}
            </p>
          </div>
          {project.technologies.length > 0 && (
            <ul
              className="flex flex-wrap gap-2"
              aria-label={`Tecnologías de ${project.name}`}
            >
              {project.technologies.map((technology) => (
                <li
                  key={technology}
                  className="rounded-full border border-slate-700 px-2.5 py-0.5 text-xs text-slate-300"
                >
                  {technology}
                </li>
              ))}
            </ul>
          )}
          <div className="mt-auto flex flex-wrap gap-3 pt-2">
            <Link
              to={`/proyectos/${project.slug}`}
              aria-label={`Ver proyecto ${project.name}`}
              className="rounded-lg bg-sky-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-sky-500 focus:outline-none focus-visible:ring-2 focus-visible:ring-sky-300 focus-visible:ring-offset-2 focus-visible:ring-offset-slate-900"
            >
              Ver proyecto
            </Link>
            {project.repositoryUrl && (
              <a
                href={project.repositoryUrl}
                target="_blank"
                rel="noopener noreferrer"
                aria-label={`Repositorio de ${project.name}`}
                className="rounded-lg border border-slate-700 px-4 py-2 text-sm font-semibold text-slate-200 transition hover:border-slate-500 focus:outline-none focus-visible:ring-2 focus-visible:ring-sky-400"
              >
                Repositorio
              </a>
            )}
          </div>
        </div>
      </article>
    </li>
  )
}

export function ContactSection({ profile }: { profile: Profile }) {
  return (
    <Section id={SECTION_IDS.contact} title="Contacto">
      <div className="flex flex-col gap-6 sm:flex-row sm:items-center sm:justify-between">
        <div className="min-w-0">
          <p className="text-slate-300">
            ¿Tienes un proyecto en mente? Escríbeme a
          </p>
          <a
            href={`mailto:${profile.email}`}
            className="mt-1 inline-block text-xl font-semibold break-all text-sky-400 hover:text-sky-300 focus:outline-none focus-visible:ring-2 focus-visible:ring-sky-400"
          >
            {profile.email}
          </a>
        </div>
        <SocialLinks links={profile.socialLinks} />
      </div>
    </Section>
  )
}

export function LandingFooter({ owner }: { owner: string }) {
  return (
    <footer className="border-t border-slate-800/80 py-8 text-center text-sm text-slate-500">
      © {new Date().getFullYear()} {owner}
    </footer>
  )
}
