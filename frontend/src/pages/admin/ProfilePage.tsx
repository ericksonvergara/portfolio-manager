import { useState } from 'react'
import {
  getAdminProfile,
  saveProfile,
  type ProfileInput,
} from '../../features/admin/profile/adminProfileApi'
import { ProfileForm } from '../../features/admin/profile/ProfileForm'
import {
  EMPTY_PROFILE,
  fromProfile,
  toProfileFormError,
} from '../../features/admin/profile/profileFormModel'
import { ProfilePhoto } from '../../features/admin/profile/ProfilePhoto'
import {
  ErrorState,
  LoadingState,
} from '../../features/admin/projects/AdminStates'
import { useAdminData } from '../../features/admin/projects/useAdminData'
import type { Profile } from '../../features/landing/landingApi'

/**
 * Edits the public profile shown on the landing page (HU023): name, headline, presentation, contact
 * email, location, social links and photo. The first save creates the profile.
 */
export function ProfilePage() {
  const content = useAdminData(getAdminProfile)
  // The latest version after a save or a photo change; until then, the loaded one.
  const [changed, setChanged] = useState<Profile | null>(null)
  const [saved, setSaved] = useState(false)

  const save = async (input: ProfileInput) => {
    setSaved(false)
    try {
      setChanged(await saveProfile(input))
      setSaved(true)
    } catch (error) {
      throw toProfileFormError(error)
    }
  }

  const loaded = content.status === 'ready' ? content.data : null
  const profile = changed ?? loaded

  return (
    <section aria-labelledby="profile-title">
      <h1 id="profile-title" className="text-2xl font-bold">
        Perfil
      </h1>
      <p className="mt-1 mb-6 text-sm text-slate-400">
        Los datos que se muestran en la portada del portafolio.
      </p>

      {content.status === 'loading' && (
        <LoadingState label="Cargando perfil…" />
      )}
      {content.status === 'error' && (
        <ErrorState
          message="No se pudo cargar el perfil."
          retry={content.retry}
        />
      )}
      {content.status === 'ready' && (
        <>
          {!loaded && !changed && (
            <p className="mb-6 rounded-lg border border-sky-900 bg-sky-950/40 px-3 py-2 text-sm text-sky-200">
              Aún no hay un perfil. Completa los datos y guárdalo para que
              aparezca en el portafolio.
            </p>
          )}
          {saved && (
            <p
              role="status"
              className="mb-6 rounded-lg border border-emerald-800 bg-emerald-950 px-3 py-2 text-sm text-emerald-200"
            >
              Perfil guardado.
            </p>
          )}
          <ProfileForm
            defaultValues={loaded ? fromProfile(loaded) : EMPTY_PROFILE}
            onSubmit={save}
          />
          <ProfilePhoto profile={profile} onChange={setChanged} />
        </>
      )}
    </section>
  )
}
