import {
  AboutSection,
  ContactSection,
  HeroSection,
  LandingFooter,
  LandingHeader,
  PortfolioSection,
  TechnologiesSection,
} from '../features/landing/LandingSections'
import { useLandingContent } from '../features/landing/useLandingContent'

const DEFAULT_BRAND = 'Portafolio'

/** Public landing page (HU001): accessible without logging in. */
export function HomePage() {
  const content = useLandingContent()

  if (content.status === 'loading') {
    return (
      <main className="flex min-h-screen items-center justify-center bg-slate-950">
        <p role="status" className="text-sm text-slate-400">
          Cargando portafolio...
        </p>
      </main>
    )
  }

  if (content.status === 'error') {
    return (
      <main className="flex min-h-screen flex-col items-center justify-center gap-4 bg-slate-950 px-4 text-center">
        <p role="alert" className="text-slate-300">
          No se pudo cargar el portafolio. Revisa tu conexión e inténtalo de
          nuevo.
        </p>
        <button
          type="button"
          onClick={content.retry}
          className="rounded-lg bg-sky-600 px-4 py-2 font-semibold text-white transition hover:bg-sky-500 focus:outline-none focus-visible:ring-2 focus-visible:ring-sky-300"
        >
          Reintentar
        </button>
      </main>
    )
  }

  const { profile, technologies, projects } = content
  const owner = profile?.fullName ?? DEFAULT_BRAND

  return (
    <div className="min-h-screen bg-slate-950 text-slate-200">
      <LandingHeader brand={owner} />
      <main id="contenido">
        <HeroSection profile={profile} />
        {profile && <AboutSection profile={profile} />}
        <TechnologiesSection technologies={technologies} />
        <PortfolioSection projects={projects} />
        {profile && <ContactSection profile={profile} />}
      </main>
      <LandingFooter owner={owner} />
    </div>
  )
}
